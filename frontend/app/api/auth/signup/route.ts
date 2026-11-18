import { NextResponse } from "next/server";
import { hash } from "bcryptjs";
import { UserRole } from "@prisma/client";
import {
  createUser,
  findUserByEmail,
  findUserByIdentity,
} from "@/repositories/userRepository";

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

    if (password.length < 8) {
      return NextResponse.json(
        { message: "Password must contain at least 8 characters." },
        { status: 400 }
      );
    }

    if (!["FACULTY", "STUDENT"].includes(role)) {
      return NextResponse.json(
        { message: "Invalid user role." },
        { status: 400 }
      );
    }

    const normalizedEmail = email.trim().toLowerCase();
    const normalizedIdentity = identity.trim();

    const existingEmail = await findUserByEmail(normalizedEmail);

    if (existingEmail) {
      return NextResponse.json(
        { message: "Email is already registered." },
        { status: 409 }
      );
    }

    const existingIdentity = await findUserByIdentity(
      normalizedIdentity,
      role as UserRole
    );

    if (existingIdentity) {
      return NextResponse.json(
        { message: "This ID or register number is already registered." },
        { status: 409 }
      );
    }

    const passwordHash = await hash(password, 12);

    await createUser({
      name: name.trim(),
      identity: normalizedIdentity,
      email: normalizedEmail,
      passwordHash,
      role: role as UserRole,
    });

    return NextResponse.json(
      {
        message: "Account created successfully! You can now login.",
      },
      { status: 201 }
    );
  } catch (error) {
    console.error("Signup error:", error);

    return NextResponse.json(
      { message: "Unable to create account. Please try again." },
      { status: 500 }
    );
  }
}