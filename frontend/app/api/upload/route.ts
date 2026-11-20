import { NextResponse } from "next/server";
import { getCurrentUser } from "@/lib/auth";

export async function POST(request: Request) {
  try {
    const currentUser = await getCurrentUser();

    if (!currentUser || !currentUser.backendToken) {
      return NextResponse.json(
        { message: "Unauthorized. Please login to upload files." },
        { status: 401 }
      );
    }

    const formData = await request.formData();
    const file = formData.get("file");

    if (!file || !(file instanceof Blob)) {
      return NextResponse.json(
        { message: "No file provided for upload." },
        { status: 400 }
      );
    }

    const backendUrl =
      process.env.BACKEND_API_URL ||
      process.env.NEXT_PUBLIC_BACKEND_URL ||
      "http://localhost:8080";

    const forwardFormData = new FormData();
    forwardFormData.append("file", file);

    const backendResponse = await fetch(
      `${backendUrl}/api/files/upload/profile-image`,
      {
        method: "POST",
        headers: {
          Authorization: `Bearer ${currentUser.backendToken}`,
        },
        body: forwardFormData,
      }
    );

    const data = await backendResponse.json();

    if (!backendResponse.ok || !data.success) {
      return NextResponse.json(
        { message: data.message || "Failed to upload file to backend." },
        { status: backendResponse.status || 500 }
      );
    }

    const imageUrl = data.data?.imageUrl;
    // Map /api/files/... to /backend-api/files/... so Next.js proxies it cleanly
    const publicUrl = imageUrl ? `/backend-api${imageUrl.replace("/api", "")}` : "";

    return NextResponse.json({
      message: "Profile image uploaded successfully.",
      imageUrl: publicUrl || imageUrl,
      fileName: data.data?.fileName,
    });
  } catch (error) {
    console.error("File upload error:", error);
    return NextResponse.json(
      { message: "Internal error processing file upload." },
      { status: 500 }
    );
  }
}
