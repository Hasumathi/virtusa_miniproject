package com.smartcampus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AttendanceNotFoundException extends ResourceNotFoundException {
    public AttendanceNotFoundException(Long id) {
        super(String.format("Attendance record not found with ID: %d", id));
    }
}
