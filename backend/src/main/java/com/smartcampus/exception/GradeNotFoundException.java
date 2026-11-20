package com.smartcampus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class GradeNotFoundException extends ResourceNotFoundException {
    public GradeNotFoundException(Long id) {
        super(String.format("Grade record not found with ID: %d", id));
    }
}
