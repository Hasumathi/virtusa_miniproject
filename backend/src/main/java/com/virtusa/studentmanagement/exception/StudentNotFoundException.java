package com.virtusa.studentmanagement.exception;

/**
 * Custom Exception thrown when a requested Student record is not found in the database.
 * Results in an HTTP 404 NOT_FOUND response.
 */
public class StudentNotFoundException extends RuntimeException {

    public StudentNotFoundException(String message) {
        super(message);
    }

    public StudentNotFoundException(Long id) {
        super("Student not found with ID: " + id);
    }
}

