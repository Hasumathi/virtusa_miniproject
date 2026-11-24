import { jwtVerify } from "jose";
import { cookies } from "next/headers";

export type SessionUser = {
  id: string;
  role: "FACULTY" | "STUDENT";
  email: string;
  name: string;
  backendToken?: string;
  facultyOrStudentId?: number | null;
};

export async function getCurrentUser(): Promise<SessionUser | null> {
  try {
    const authSecret =
      process.env.AUTH_SECRET ||
      "smartcampussecretkeymustbeatleast32byteslong123456";

    const cookieStore = await cookies();
    const token = cookieStore.get("edutrack_session")?.value;

    if (!token) {
      return null;
    }

    const secret = new TextEncoder().encode(authSecret);
    const { payload } = await jwtVerify(token, secret);

    const role = (payload.role as string)?.toUpperCase();
    if (!role || !["FACULTY", "STUDENT"].includes(role)) {
      return null;
    }

    return {
      id: String(payload.sub || payload.userId || ""),
      role: role as "FACULTY" | "STUDENT",
      email: (payload.email as string) || "",
      name: (payload.name as string) || "",
      backendToken: (payload.backendToken as string) || undefined,
      facultyOrStudentId: (payload.facultyOrStudentId as number) || null,
    };
  } catch {
    return null;
  }
}
