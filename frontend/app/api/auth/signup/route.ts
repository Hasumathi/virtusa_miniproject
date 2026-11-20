import { NextResponse } from "next/server";

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const { name, identity, email, password, role } = body;

    if (
      typeof name !== "string" ||
      typeof identity !== "string" ||
      typeof email !== "string" ||
      typeof password !== "string" ||
      !name.trim() ||
      !identity.trim() ||
      !email.trim() ||
      !password
    ) {
      return NextResponse.json(
        { message: "All fields are required." },
        { status: 400 }
      );
    }

    if (name.trim().length < 2) {
      return NextResponse.json(
        { message: "Name must contain at least 2 characters." },
        { status: 400 }
      );
    }

    if (password.length < 6) {
      return NextResponse.json(
        { message: "Password must contain at least 6 characters." },
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

    const backendUrl =
      process.env.BACKEND_API_URL ||
      process.env.NEXT_PUBLIC_BACKEND_URL ||
      "http://localhost:8080";

    let backendResponse: Response;

    if (requestedRole === "FACULTY") {
      const username = email.includes("@")
        ? email.split("@")[0].replace(/[^a-zA-Z0-9_]/g, "")
        : identity.trim();

      backendResponse = await fetch(`${backendUrl}/api/auth/faculty/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          employeeId: identity.trim(),
          username,
          fullName: name.trim(),
          email: email.trim().toLowerCase(),
          password,
          departmentId: 1, // Default CSE Department
          authorizationKey: "FACULTY_SECRET_KEY_2026",
        }),
      });
    } else {
      // STUDENT Registration
      backendResponse = await fetch(`${backendUrl}/api/auth/student/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          registerNumber: identity.trim().toUpperCase(),
          fullName: name.trim(),
          email: email.trim().toLowerCase(),
          password,
          departmentId: 1, // Default CSE Department
        }),
      });
    }

    const data = await backendResponse.json();

    if (!backendResponse.ok || !data.success) {
      const errorMsg =
        data.message ||
        (data.validationErrors
          ? Object.values(data.validationErrors).join(", ")
          : "Registration failed. Please check your details.");
      return NextResponse.json(
        { message: errorMsg },
        { status: backendResponse.status || 400 }
      );
    }

    return NextResponse.json({
      message: "Account created successfully! You can now login.",
      data: data.data,
    });
  } catch (error) {
    console.error("Signup error:", error);
    return NextResponse.json(
      {
        message:
          "Unable to connect to registration service. Please verify that the backend is running.",
      },
      { status: 503 }
    );
  }
}
