// API service functions for Add / Edit / Delete (Member 2).
// Member 1's GET request stays in app/page.tsx.
//
// Backend contract (verified from StudentController.java):
//   POST   /api/students       body: student JSON      -> 200 + created Student (JSON)
//   PUT    /api/students/{id}  body: student JSON      -> 200 + updated Student (JSON)
//   DELETE /api/students/{id}                          -> 200 + PLAIN TEXT (not JSON!)
//   Errors: 400 invalid data, 404 not found, 409 duplicate email.

import type { Student, StudentInput } from "./types";

export const API_URL =
  process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api/students";

export type ApiErrorKind =
  | "network"
  | "validation"
  | "duplicate"
  | "not-found"
  | "server"
  | "unknown";

export class ApiError extends Error {
  kind: ApiErrorKind;
  status?: number;

  constructor(kind: ApiErrorKind, message: string, status?: number) {
    super(message);
    this.name = "ApiError";
    this.kind = kind;
    this.status = status;
  }
}

// Reads a "message" from the backend error body if there is one.
// NOTE: Member 5's final ErrorResponse format is not in the repo yet, so we only
// look for a plain "message" text and otherwise fall back to the HTTP status.
// When Member 5 finishes, adjust ONLY this function.
async function readServerMessage(response: Response): Promise<string | undefined> {
  try {
    const text = await response.text();
    const body: unknown = JSON.parse(text);
    if (body && typeof body === "object" && "message" in body) {
      const message = (body as { message: unknown }).message;
      if (
        typeof message === "string" &&
        message.trim() !== "" &&
        message !== "No message available"
      ) {
        return message;
      }
    }
  } catch {
    // Body was empty or not JSON: ignore and use the status-based message.
  }
  return undefined;
}

async function buildError(response: Response): Promise<ApiError> {
  const serverMessage = await readServerMessage(response);
  const status = response.status;

  if (status === 400) {
    return new ApiError(
      "validation",
      serverMessage ?? "Some details are invalid. Please check the form and try again.",
      status
    );
  }
  if (status === 404) {
    return new ApiError(
      "not-found",
      serverMessage ?? "This student no longer exists. It may have been deleted already.",
      status
    );
  }
  if (status === 409) {
    return new ApiError(
      "duplicate",
      serverMessage ?? "A student with this email already exists.",
      status
    );
  }
  if (status >= 500) {
    return new ApiError(
      "server",
      "The server ran into a problem. Please try again in a moment.",
      status
    );
  }
  return new ApiError("unknown", serverMessage ?? `Request failed (status ${status}).`, status);
}

// One place that sends the request and turns every failure into an ApiError.
async function send(url: string, init: RequestInit): Promise<Response> {
  let response: Response;
  try {
    response = await fetch(url, init);
  } catch {
    // fetch only throws when the server cannot be reached at all.
    throw new ApiError(
      "network",
      "Cannot reach the server. Make sure the backend is running on port 8080 and try again."
    );
  }

  if (!response.ok) {
    throw await buildError(response);
  }
  return response;
}

const JSON_HEADERS = {
  "Content-Type": "application/json",
  Accept: "application/json",
};

// POST: create a student
export async function createStudent(input: StudentInput): Promise<Student> {
  const response = await send(API_URL, {
    method: "POST",
    headers: JSON_HEADERS,
    body: JSON.stringify(input),
  });
  return (await response.json()) as Student;
}

// PUT: update an existing student
export async function updateStudent(id: number, input: StudentInput): Promise<Student> {
  const response = await send(`${API_URL}/${id}`, {
    method: "PUT",
    headers: JSON_HEADERS,
    body: JSON.stringify(input),
  });
  return (await response.json()) as Student;
}

// DELETE: remove a student. The backend answers with plain text, so no .json() here.
export async function deleteStudent(id: number): Promise<string> {
  const response = await send(`${API_URL}/${id}`, { method: "DELETE" });
  return await response.text();
}
