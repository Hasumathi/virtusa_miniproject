import { NextResponse } from "next/server";
import { getCurrentUser } from "@/lib/auth";
import {
  findUserById,
  updateUserProfile,
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

    const { passwordHash, ...safeUser } = user;

    return NextResponse.json({ user: safeUser });
  } catch {
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

    const allowedFields = [
      "name",
      "phone",
      "department",
      "bio",
      "profileImageUrl",
    ];

    if (
      !body ||
      typeof body !== "object" ||
      Array.isArray(body) ||
      !Object.keys(body).some((key) => allowedFields.includes(key)) ||
      Object.keys(body).some((key) => !allowedFields.includes(key))
    ) {
      return NextResponse.json(
        { message: "Invalid profile details." },
        { status: 400 }
      );
    }

    if (
      body.name !== undefined &&
      (typeof body.name !== "string" || body.name.trim().length < 2)
    ) {
      return NextResponse.json(
        { message: "Name must contain at least 2 characters." },
        { status: 400 }
      );
    }

    if (
      body.phone !== undefined &&
      body.phone !== null &&
      (typeof body.phone !== "string" || body.phone.length > 20)
    ) {
      return NextResponse.json(
        { message: "Invalid phone number." },
        { status: 400 }
      );
    }

    if (
      body.department !== undefined &&
      body.department !== null &&
      (typeof body.department !== "string" || body.department.length > 100)
    ) {
      return NextResponse.json(
        { message: "Department must be within 100 characters." },
        { status: 400 }
      );
    }

    if (
      body.bio !== undefined &&
      body.bio !== null &&
      typeof body.bio !== "string"
    ) {
      return NextResponse.json(
        { message: "Invalid bio." },
        { status: 400 }
      );
    }

    if (
      body.profileImageUrl !== undefined &&
      body.profileImageUrl !== null &&
      (typeof body.profileImageUrl !== "string" ||
        body.profileImageUrl.length > 500)
    ) {
      return NextResponse.json(
        { message: "Invalid profile image URL." },
        { status: 400 }
      );
    }

    const updatedUser = await updateUserProfile(currentUser.id, {
      ...(body.name !== undefined ? { name: body.name.trim() } : {}),
      ...(body.phone !== undefined ? { phone: body.phone } : {}),
      ...(body.department !== undefined
        ? { department: body.department }
        : {}),
      ...(body.bio !== undefined ? { bio: body.bio } : {}),
      ...(body.profileImageUrl !== undefined
        ? { profileImageUrl: body.profileImageUrl }
        : {}),
    });

    const { passwordHash, ...safeUser } = updatedUser;

    return NextResponse.json({
      message: "Profile updated successfully.",
      user: safeUser,
    });
  } catch {
    return NextResponse.json(
      { message: "Unable to update profile." },
      { status: 500 }
    );
  }
}