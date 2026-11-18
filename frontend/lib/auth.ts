
import { jwtVerify } from "jose";
import { cookies } from "next/headers";

export async function getCurrentUser() {
  try {
    const authSecret = process.env.AUTH_SECRET;

    if (!authSecret) {
      return null;
    }

    const cookieStore = await cookies();
    const token = cookieStore.get("edutrack_session")?.value;

    if (!token) {
      return null;
    }

    const secret = new TextEncoder().encode(authSecret);

    const { payload } = await jwtVerify(token, secret);

    if (
      typeof payload.sub !== "string" ||
      !["FACULTY", "STUDENT"].includes(payload.role as string)
    ) {
      return null;
    }

    return {
      id: payload.sub,
      role: payload.role as "FACULTY" | "STUDENT",
      email: payload.email as string,
      name: payload.name as string,
    };
  } catch {
    return null;
  }
}
