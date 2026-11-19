package com.smartcampus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SubjectNotFoundException extends ResourceNotFoundException {
    public SubjectNotFoundException(Long id) {
        super(String.format("Subject not found with ID: %d", id));
    }

    public SubjectNotFoundException(String code) {
        super(String.format("Subject not found with code: %s", code));
    }
}
