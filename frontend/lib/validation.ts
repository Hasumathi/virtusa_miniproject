// Frontend form validation (Member 2).
// Rules copied from the backend Student entity so the frontend and backend agree:
//   name        required, 2-100 characters
//   email       required, valid email format
//   department  required (max 100 characters)
//   year        required, whole number 1-4
//   phone       required, exactly 10 digits

import type { Student, StudentInput } from "./types";

// Form fields are kept as strings because that is what <input> gives us.
export type StudentFormValues = {
  name: string;
  email: string;
  department: string;
  yearOfStudy: string;
  phoneNumber: string;
};

export type FieldName = keyof StudentFormValues;
export type FieldErrors = Partial<Record<FieldName, string>>;

export const EMPTY_FORM: StudentFormValues = {
  name: "",
  email: "",
  department: "",
  yearOfStudy: "",
  phoneNumber: "",
};

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
const PHONE_PATTERN = /^[0-9]{10}$/;
const YEAR_PATTERN = /^[1-4]$/;

export function validateStudentForm(values: StudentFormValues): FieldErrors {
  const errors: FieldErrors = {};

  const name = values.name.trim();
  if (!name) {
    errors.name = "Student name is required.";
  } else if (name.length < 2 || name.length > 100) {
    errors.name = "Name must be between 2 and 100 characters.";
  }

  const email = values.email.trim();
  if (!email) {
    errors.email = "Email is required.";
  } else if (email.length > 150) {
    errors.email = "Email must not exceed 150 characters.";
  } else if (!EMAIL_PATTERN.test(email)) {
    errors.email = "Enter a valid email address, e.g. name@example.com.";
  }

  const department = values.department.trim();
  if (!department) {
    errors.department = "Department is required.";
  } else if (department.length > 100) {
    errors.department = "Department must not exceed 100 characters.";
  }

  const year = values.yearOfStudy.trim();
  if (!year) {
    errors.yearOfStudy = "Year of study is required.";
  } else if (!YEAR_PATTERN.test(year)) {
    errors.yearOfStudy = "Year of study must be a whole number from 1 to 4.";
  }

  const phone = values.phoneNumber.trim();
  if (!phone) {
    errors.phoneNumber = "Phone number is required.";
  } else if (!PHONE_PATTERN.test(phone)) {
    errors.phoneNumber = "Phone number must be exactly 10 digits (numbers only).";
  }

  return errors;
}

// Convert the (valid) form strings into the object the backend expects.
export function toStudentInput(values: StudentFormValues): StudentInput {
  return {
    name: values.name.trim(),
    email: values.email.trim(),
    department: values.department.trim(),
    yearOfStudy: Number(values.yearOfStudy),
    phoneNumber: values.phoneNumber.trim(),
  };
}

// Fill the Edit form with an existing student's details.
export function studentToForm(student: Student): StudentFormValues {
  return {
    name: student.name,
    email: student.email,
    department: student.department,
    yearOfStudy: String(student.yearOfStudy),
    phoneNumber: student.phoneNumber,
  };
}
