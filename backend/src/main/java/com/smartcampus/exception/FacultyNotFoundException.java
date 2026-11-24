package com.smartcampus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class FacultyNotFoundException extends ResourceNotFoundException {
    public FacultyNotFoundException(Long id) {
        super(String.format("Faculty not found with ID: %d", id));
    }

    public FacultyNotFoundException(String employeeId) {
        super(String.format("Faculty not found with employee ID: %s", employeeId));
    }
}
