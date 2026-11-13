"use client";

// Add Student / Edit Student form (Member 2).
// One component is used for both: mode="add" starts empty,
// mode="edit" is pre-filled with the selected student's details.

import { useEffect, useId, useRef, useState } from "react";
import type { FormEvent } from "react";
import { createStudent, updateStudent, ApiError } from "@/lib/api";
import type { Student } from "@/lib/types";
import {
  EMPTY_FORM,
  studentToForm,
  toStudentInput,
  validateStudentForm,
} from "@/lib/validation";
import type { FieldName, StudentFormValues } from "@/lib/validation";

type Props = {
  mode: "add" | "edit";
  student?: Student; // required when mode === "edit"
  departments: string[]; // existing departments, used as suggestions
  onClose: () => void;
  onSaved: (message: string) => void; // parent shows the toast and refreshes the list
};

export default function StudentFormModal({
  mode,
  student,
  departments,
  onClose,
  onSaved,
}: Props) {
  const uid = useId();
  const [values, setValues] = useState<StudentFormValues>(
    mode === "edit" && student ? studentToForm(student) : EMPTY_FORM
  );
  const [touched, setTouched] = useState<Partial<Record<FieldName, boolean>>>({});
  const [submitAttempted, setSubmitAttempted] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState("");
  const [emailServerError, setEmailServerError] = useState("");
  const inFlight = useRef(false); // blocks double submits even before React re-renders

  const errors = validateStudentForm(values);

  // Close with the Escape key (but not while a request is running).
  useEffect(() => {
    function onKeyDown(e: KeyboardEvent) {
      if (e.key === "Escape" && !inFlight.current) onClose();
    }
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [onClose]);

  function setField(field: FieldName, value: string) {
    setValues((prev) => ({ ...prev, [field]: value }));
    if (field === "email") setEmailServerError("");
  }

  function fieldError(field: FieldName): string | undefined {
    if (field === "email" && emailServerError) return emailServerError;
    return touched[field] || submitAttempted ? errors[field] : undefined;
  }

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (inFlight.current) return;

    setSubmitAttempted(true);
    setServerError("");

    if (Object.keys(errors).length > 0) return; // show messages, do not call the API

    inFlight.current = true;
    setSubmitting(true);

    try {
      const input = toStudentInput(values);

      if (mode === "edit" && student) {
        await updateStudent(student.id, input);
        onSaved(`${input.name} was updated successfully.`);
      } else {
        await createStudent(input);
        onSaved(`${input.name} was added successfully.`);
      }
      // Success: the parent closes this modal, so we do not touch state here.
    } catch (err) {
      inFlight.current = false;
      setSubmitting(false);

      if (err instanceof ApiError) {
        if (err.kind === "duplicate") {
          setEmailServerError(err.message); // show it right under the email field
        } else {
          setServerError(err.message);
        }
      } else {
        setServerError("Something unexpected went wrong. Please try again.");
      }
      // The form keeps what the user typed.
    }
  }

  const title = mode === "edit" ? "Edit Student" : "Add New Student";
  const id = (field: string) => `${uid}-${field}`;

  function renderError(field: FieldName) {
    const message = fieldError(field);
    return message ? (
      <p className="field-error" id={id(`${field}-error`)} role="alert">
        {message}
      </p>
    ) : null;
  }

  return (
    <div
      className="modal-overlay"
      onMouseDown={(e) => {
        if (e.target === e.currentTarget && !submitting) onClose();
      }}
    >
      <div className="modal" role="dialog" aria-modal="true" aria-labelledby={id("title")}>
        <div className="modal-header">
          <div>
            <h2 id={id("title")}>{title}</h2>
            <p>
              {mode === "edit"
                ? "Update the details below and save your changes."
                : "Fill in the details to register a new student."}
            </p>
          </div>
          <button
            type="button"
            className="modal-close"
            aria-label="Close"
            onClick={onClose}
            disabled={submitting}
          >
            ✕
          </button>
        </div>

        <form onSubmit={handleSubmit} noValidate>
          {serverError && (
            <div className="form-alert" role="alert">
              {serverError}
            </div>
          )}

          <div className="form-grid">
            <div className="form-field form-field-full">
              <label htmlFor={id("name")}>Student Name</label>
              <input
                id={id("name")}
                type="text"
                placeholder="e.g. Aarav Sharma"
                value={values.name}
                onChange={(e) => setField("name", e.target.value)}
                onBlur={() => setTouched((t) => ({ ...t, name: true }))}
                aria-invalid={!!fieldError("name")}
                disabled={submitting}
                autoFocus
              />
              {renderError("name")}
            </div>

            <div className="form-field form-field-full">
              <label htmlFor={id("email")}>Email</label>
              <input
                id={id("email")}
                type="email"
                placeholder="name@example.com"
                value={values.email}
                onChange={(e) => setField("email", e.target.value)}
                onBlur={() => setTouched((t) => ({ ...t, email: true }))}
                aria-invalid={!!fieldError("email")}
                disabled={submitting}
              />
              {renderError("email")}
            </div>

            <div className="form-field">
              <label htmlFor={id("department")}>Department</label>
              <input
                id={id("department")}
                type="text"
                list={id("departments")}
                placeholder="e.g. Computer Science"
                value={values.department}
                onChange={(e) => setField("department", e.target.value)}
                onBlur={() => setTouched((t) => ({ ...t, department: true }))}
                aria-invalid={!!fieldError("department")}
                disabled={submitting}
              />
              <datalist id={id("departments")}>
                {departments.map((d) => (
                  <option key={d} value={d} />
                ))}
              </datalist>
              {renderError("department")}
            </div>

            <div className="form-field">
              <label htmlFor={id("year")}>Year of Study</label>
              <select
                id={id("year")}
                value={values.yearOfStudy}
                onChange={(e) => setField("yearOfStudy", e.target.value)}
                onBlur={() => setTouched((t) => ({ ...t, yearOfStudy: true }))}
                aria-invalid={!!fieldError("yearOfStudy")}
                disabled={submitting}
              >
                <option value="">Select year</option>
                <option value="1">1st Year</option>
                <option value="2">2nd Year</option>
                <option value="3">3rd Year</option>
                <option value="4">4th Year</option>
              </select>
              {renderError("yearOfStudy")}
            </div>

            <div className="form-field form-field-full">
              <label htmlFor={id("phone")}>Phone Number</label>
              <input
                id={id("phone")}
                type="tel"
                inputMode="numeric"
                maxLength={10}
                placeholder="10-digit number"
                value={values.phoneNumber}
                onChange={(e) => setField("phoneNumber", e.target.value)}
                onBlur={() => setTouched((t) => ({ ...t, phoneNumber: true }))}
                aria-invalid={!!fieldError("phoneNumber")}
                disabled={submitting}
              />
              {renderError("phoneNumber")}
            </div>
          </div>

          <div className="modal-actions">
            <button
              type="button"
              className="btn btn-secondary"
              onClick={onClose}
              disabled={submitting}
            >
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting
                ? mode === "edit"
                  ? "Saving..."
                  : "Adding..."
                : mode === "edit"
                  ? "Save Changes"
                  : "Add Student"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
