import { NextResponse } from "next/server";
import { getCurrentUser } from "@/lib/auth";
import {
  findUserById,
  updateUserSettings,
} from "@/repositories/userRepository";

export async function GET() {
  try {
    const currentUser = await getCurrentUser();

    if (!currentUser) {
      return NextResponse.json(
        { message: "Unauthorized. Please login." },
        { status: 401 }
      );
    }

    const user = await findUserById(currentUser.id);

    if (!user) {
      return NextResponse.json(
        { message: "User not found." },
        { status: 404 }
      );
    }

    return NextResponse.json({
      settings: {
        emailNotifications:
          user.profileSettings?.emailNotifications ?? true,
        pushNotifications:
          user.profileSettings?.pushNotifications ?? true,
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

    const allowedFields = [
      "emailNotifications",
      "pushNotifications",
    ];

    if (
      !body ||
      typeof body !== "object" ||
      Array.isArray(body) ||
      Object.keys(body).length === 0 ||
      Object.keys(body).some((key) => !allowedFields.includes(key)) ||
      Object.values(body).some((value) => typeof value !== "boolean")
    ) {
      return NextResponse.json(
        { message: "Invalid settings data." },
        { status: 400 }
      );
    }

    const settings = await updateUserSettings(
      currentUser.id,
      {
        ...(body.emailNotifications !== undefined
          ? { emailNotifications: body.emailNotifications }
          : {}),
        ...(body.pushNotifications !== undefined
          ? { pushNotifications: body.pushNotifications }
          : {}),
      }
    );

    return NextResponse.json({
      message: "Settings updated successfully.",
      settings: {
        emailNotifications: settings.emailNotifications,
        pushNotifications: settings.pushNotifications,
      },
    });
  } catch {
    return NextResponse.json(
      { message: "Unable to update settings." },
      { status: 500 }
    );
  }
}