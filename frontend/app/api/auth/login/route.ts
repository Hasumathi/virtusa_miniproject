
import { NextResponse } from "next/server";
import { compare } from "bcryptjs";
import { SignJWT } from "jose";
import { UserRole } from "@prisma/client";
import { findUserByEmail } from "@/repositories/userRepository";

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

    if (!["FACULTY", "STUDENT"].includes(role)) {
      return NextResponse.json(
        { message: "Invalid user role." },
        { status: 400 }
      );
    }

    const authSecret = process.env.AUTH_SECRET;

    if (!authSecret) {
      console.error("AUTH_SECRET is missing.");
      return NextResponse.json(
        { message: "Server configuration error." },
        { status: 500 }
      );
    }

    const user = await findUserByEmail(email.trim().toLowerCase());

    if (
      !user ||
      user.role !== (role as UserRole) ||
      !(await compare(password, user.passwordHash))
    ) {
      return NextResponse.json(
        { message: "Invalid email, password, or account type." },
        { status: 401 }
      );
    }

    const secret = new TextEncoder().encode(authSecret);

    const token = await new SignJWT({
      role: user.role,
      name: user.name,
      email: user.email,
    })
      .setProtectedHeader({ alg: "HS256" })
      .setSubject(user.id)
      .setIssuedAt()
      .setExpirationTime("7d")
      .sign(secret);

    const response = NextResponse.json({
      message: "Login successful.",
      user: {
        id: user.id,
        name: user.name,
        email: user.email,
        role: user.role,
      },
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
    console.error("Login error:", error);

    return NextResponse.json(
      { message: "Unable to login. Please try again." },
      { status: 500 }
    );
  }
}
