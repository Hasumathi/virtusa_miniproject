package com.smartcampus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(String identifier) {
        super(String.format("User not found with identifier: %s", identifier));
    }
}
