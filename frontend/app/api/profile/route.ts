import { NextResponse } from "next/server";
import { getCurrentUser } from "@/lib/auth";

export async function GET() {
  try {
    const currentUser = await getCurrentUser();

    if (!currentUser) {
      return NextResponse.json(
        { message: "Unauthorized. Please login." },
        { status: 401 }
      );
    }

    const backendUrl =
      process.env.BACKEND_API_URL ||
      process.env.NEXT_PUBLIC_BACKEND_URL ||
      "http://localhost:8080";

    if (currentUser.backendToken) {
      const endpoint =
        currentUser.role === "FACULTY"
          ? `${backendUrl}/api/faculty/profile`
          : `${backendUrl}/api/students/reg/${currentUser.name}`;

      try {
        const response = await fetch(endpoint, {
          headers: {
            Authorization: `Bearer ${currentUser.backendToken}`,
          },
        });

        if (response.ok) {
          const result = await response.json();
          const profileData = result.data || {};
          return NextResponse.json({
            user: {
              id: currentUser.id,
              name: profileData.fullName || currentUser.name,
              email: profileData.email || currentUser.email,
              identity:
                profileData.employeeId ||
                profileData.registerNumber ||
                currentUser.name,
              role: currentUser.role,
              profileSettings: {
                phone: profileData.phone || null,
                department:
                  profileData.departmentName ||
                  profileData.department?.name ||
                  null,
                bio: profileData.designation || null,
                profileImageUrl: profileData.profileImageUrl || null,
              },
            },
          });
        }
      } catch (err) {
        console.warn("Backend profile fetch fallback:", err);
      }
    }

    // Fallback using session info
    return NextResponse.json({
      user: {
        id: currentUser.id,
        name: currentUser.name,
        email: currentUser.email,
        identity: currentUser.name,
        role: currentUser.role,
        profileSettings: {
          phone: null,
          department: null,
          bio: null,
          profileImageUrl: null,
        },
      },
    });
  } catch (error) {
    console.error("Profile route error:", error);
    return NextResponse.json(
      { message: "Unable to fetch profile." },
      { status: 500 }
    );
  }
}

export async function PATCH(request: Request) {
  try {
    const currentUser = await getCurrentUser();

    if (!currentUser) {
      return NextResponse.json(
        { message: "Unauthorized. Please login." },
        { status: 401 }
      );
    }

    const body = await request.json();
    const backendUrl =
      process.env.BACKEND_API_URL ||
      process.env.NEXT_PUBLIC_BACKEND_URL ||
      "http://localhost:8080";

    if (currentUser.backendToken && currentUser.role === "FACULTY") {
      try {
        await fetch(`${backendUrl}/api/faculty/profile`, {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${currentUser.backendToken}`,
          },
          body: JSON.stringify({
            fullName: body.name || currentUser.name,
            phone: body.phone,
            designation: body.bio,
          }),
        });
      } catch (e) {
        console.warn("Could not sync with backend profile:", e);
      }
    }

    return NextResponse.json({
      message: "Profile updated successfully.",
      user: {
        id: currentUser.id,
        name: body.name || currentUser.name,
        email: currentUser.email,
        identity: currentUser.name,
        role: currentUser.role,
        profileSettings: {
          phone: body.phone || null,
          department: body.department || null,
          bio: body.bio || null,
          profileImageUrl: body.profileImageUrl || null,
        },
      },
    });
  } catch (error) {
    console.error("Profile update error:", error);
    return NextResponse.json(
      { message: "Unable to update profile." },
      { status: 500 }
    );
  }
}
