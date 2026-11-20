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
      try {
        const response = await fetch(`${backendUrl}/api/settings`, {
          headers: {
            Authorization: `Bearer ${currentUser.backendToken}`,
          },
        });

        if (response.ok) {
          const result = await response.json();
          const s = result.data || {};
          return NextResponse.json({
            settings: {
              themePreference: s.themePreference || "SYSTEM",
              emailNotifications: s.emailNotificationsEnabled ?? true,
              pushNotifications: s.pushNotificationsEnabled ?? true,
            },
          });
        }
      } catch (err) {
        console.warn("Backend settings fetch error:", err);
      }
    }

    return NextResponse.json({
      settings: {
        themePreference: "SYSTEM",
        emailNotifications: true,
        pushNotifications: true,
      },
    });
  } catch {
    return NextResponse.json(
      { message: "Unable to fetch settings." },
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

    const themePref = body.themePreference || "SYSTEM";
    const emailNotifs = body.emailNotifications ?? body.emailNotificationsEnabled ?? true;
    const pushNotifs = body.pushNotifications ?? body.pushNotificationsEnabled ?? true;

    if (currentUser.backendToken) {
      try {
        await fetch(`${backendUrl}/api/settings`, {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${currentUser.backendToken}`,
          },
          body: JSON.stringify({
            themePreference: themePref,
            emailNotificationsEnabled: Boolean(emailNotifs),
            pushNotificationsEnabled: Boolean(pushNotifs),
          }),
        });
      } catch (err) {
        console.warn("Could not sync settings to backend:", err);
      }
    }

    return NextResponse.json({
      message: "Settings updated successfully.",
      settings: {
        themePreference: themePref,
        emailNotifications: Boolean(emailNotifs),
        pushNotifications: Boolean(pushNotifs),
      },
    });
  } catch {
    return NextResponse.json(
      { message: "Unable to update settings." },
      { status: 500 }
    );
  }
}
