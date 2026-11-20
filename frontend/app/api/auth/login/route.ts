import { NextResponse } from "next/server";
import { SignJWT } from "jose";

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const { email, password, role } = body;

    if (
      typeof email !== "string" ||
      typeof password !== "string" ||
      !email.trim() ||
      !password ||
      !role
    ) {
      return NextResponse.json(
        { message: "Email and password are required." },
        { status: 400 }
      );
    }

    const requestedRole = String(role).toUpperCase();
    if (!["FACULTY", "STUDENT"].includes(requestedRole)) {
      return NextResponse.json(
        { message: "Invalid user role specified." },
        { status: 400 }
      );
    }

    const emailTrimmed = email.trim().toLowerCase();
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(emailTrimmed)) {
      return NextResponse.json(
        {
          message:
            "Please provide a valid email address. Only emails are accepted for login.",
        },
        { status: 400 }
      );
    }

    const authSecret =
      process.env.AUTH_SECRET ||
      "smartcampussecretkeymustbeatleast32byteslong123456";
    const backendUrl =
      process.env.BACKEND_API_URL ||
      process.env.NEXT_PUBLIC_BACKEND_URL ||
      "http://localhost:8080";

    // Call Spring Boot REST API
    let backendResponse: Response;
    try {
      backendResponse = await fetch(`${backendUrl}/api/auth/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          usernameOrEmail: email.trim(),
          password: password,
        }),
      });
    } catch {
      return NextResponse.json(
        {
          message:
            "Cannot connect to Spring Boot backend. Please ensure the backend is running on port 8080.",
        },
        { status: 503 }
      );
    }

    const backendData = await backendResponse.json();

    if (!backendResponse.ok || !backendData.success) {
      const errorMessage =
        backendData.message ||
        "Invalid username/email or password provided.";
      return NextResponse.json(
        { message: errorMessage },
        { status: backendResponse.status || 401 }
      );
    }

    const authData = backendData.data;
    const userRole = (authData.role as string)?.toUpperCase();

    // Check role match
    if (userRole !== requestedRole) {
      const properPortal =
        userRole === "FACULTY" ? "Faculty" : "Student";
      return NextResponse.json(
        {
          message: `This account is registered as a ${properPortal}. Please use the ${properPortal} Login portal.`,
        },
        { status: 403 }
      );
    }

    let fullName = authData.username || email.split("@")[0];

    // For students, fetch student's actual full name rather than roll number
    if (userRole === "STUDENT" && authData.accessToken) {
      try {
        const studentDashboardRes = await fetch(
          `${backendUrl}/api/dashboard/student`,
          {
            headers: { Authorization: `Bearer ${authData.accessToken}` },
          }
        );
        if (studentDashboardRes.ok) {
          const dashJson = await studentDashboardRes.json();
          if (dashJson.data?.studentName) {
            fullName = dashJson.data.studentName;
          }
        }
      } catch (err) {
        console.warn("Could not fetch student full name on login:", err);
      }
    }

    const secret = new TextEncoder().encode(authSecret);

    const token = await new SignJWT({
      role: userRole,
      name: fullName,
      email: authData.email || email.trim(),
      userId: String(authData.userId),
      backendToken: authData.accessToken,
      facultyOrStudentId: authData.facultyOrStudentId,
    })
      .setProtectedHeader({ alg: "HS256" })
      .setSubject(String(authData.userId))
      .setIssuedAt()
      .setExpirationTime("7d")
      .sign(secret);

    const response = NextResponse.json({
      message: "Login successful.",
      user: {
        id: authData.userId,
        name: fullName,
        email: authData.email,
        role: userRole,
        facultyOrStudentId: authData.facultyOrStudentId,
      },
      accessToken: authData.accessToken,
    });

    response.cookies.set("edutrack_session", token, {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "lax",
      path: "/",
      maxAge: 60 * 60 * 24 * 7,
    });

    return response;
  } catch (error) {
    console.error("Login processing error:", error);
    return NextResponse.json(
      { message: "Unable to process login. Please try again." },
      { status: 500 }
    );
  }
}
