package com.virtusa.studentmanagement.exception;

/**
 * Custom Exception thrown when an operation attempts to register or update a student
 * with an email address that is already registered to another student.
 * Results in an HTTP 409 CONFLICT response.
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }

    public DuplicateEmailException(String email, String action) {
        super(String.format("Failed to %s student: A student with email '%s' already exists", action, email));
    }
}

