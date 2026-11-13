"use client";

// Delete confirmation dialog (Member 2).
// Shows who will be deleted and only calls DELETE after the user confirms.

import { useEffect, useRef, useState } from "react";
import { deleteStudent, ApiError } from "@/lib/api";
import type { Student } from "@/lib/types";

export type ToastMessage = { type: "success" | "error" | "info"; text: string };

type Props = {
  student: Student;
  onClose: () => void;
  onDone: (toast: ToastMessage) => void; // parent shows the toast and refreshes the list
};

export default function DeleteConfirmDialog({ student, onClose, onDone }: Props) {
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const inFlight = useRef(false);

  useEffect(() => {
    function onKeyDown(e: KeyboardEvent) {
      if (e.key === "Escape" && !inFlight.current) onClose();
    }
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [onClose]);

  async function handleConfirm() {
    if (inFlight.current) return;
    inFlight.current = true;
    setSubmitting(true);
    setError("");

    try {
      await deleteStudent(student.id);
      onDone({ type: "success", text: `${student.name} was deleted successfully.` });
    } catch (err) {
      if (err instanceof ApiError && err.kind === "not-found") {
        // Someone already removed this student: refresh the list instead of failing.
        onDone({
          type: "info",
          text: `${student.name} was already removed. The list has been refreshed.`,
        });
        return;
      }
      inFlight.current = false;
      setSubmitting(false);
      setError(
        err instanceof ApiError
          ? err.message
          : "Something unexpected went wrong. Please try again."
      );
    }
  }

  return (
    <div
      className="modal-overlay"
      onMouseDown={(e) => {
        if (e.target === e.currentTarget && !submitting) onClose();
      }}
    >
      <div
        className="modal modal-small"
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="delete-title"
        aria-describedby="delete-desc"
      >
        <div className="delete-icon">!</div>
        <h2 id="delete-title">Delete student?</h2>
        <p id="delete-desc" className="delete-text">
          You are about to delete <strong>{student.name}</strong>
          <br />
          <span className="delete-meta">{student.email}</span>
          <br />
          This action cannot be undone.
        </p>

        {error && (
          <div className="form-alert" role="alert">
            {error}
          </div>
        )}

        <div className="modal-actions modal-actions-center">
          <button
            type="button"
            className="btn btn-secondary"
            onClick={onClose}
            disabled={submitting}
            autoFocus
          >
            Cancel
          </button>
          <button
            type="button"
            className="btn btn-danger"
            onClick={handleConfirm}
            disabled={submitting}
          >
            {submitting ? "Deleting..." : "Yes, Delete"}
          </button>
        </div>
      </div>
    </div>
  );
}
