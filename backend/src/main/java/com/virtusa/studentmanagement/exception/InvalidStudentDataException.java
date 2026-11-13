package com.virtusa.studentmanagement.exception;

/**
 * Custom Exception thrown when invalid student payload or business validation fails.
 * Results in an HTTP 400 BAD_REQUEST response.
 */
public class InvalidStudentDataException extends RuntimeException {

    public InvalidStudentDataException(String message) {
        super(message);
    }
}

