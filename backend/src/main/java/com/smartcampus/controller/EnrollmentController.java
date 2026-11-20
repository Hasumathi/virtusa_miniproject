package com.smartcampus.controller;

import com.smartcampus.dto.request.BatchEnrollmentRequest;
import com.smartcampus.dto.request.EnrollmentRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.EnrollmentResponse;
import com.smartcampus.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@Tag(name = "Enrollments", description = "Student-Subject Enrollment Management APIs")
@SecurityRequirement(name = "BearerAuth")
public class EnrollmentController {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentController.class);

    private final EnrollmentService enrollmentService;

    @Autowired
    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Enroll a student in a subject (Faculty only)")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrollStudent(
            @Valid @RequestBody EnrollmentRequest request) {
        log.info("REST request to enroll student {} in subject {}", request.getStudentId(), request.getSubjectId());
        EnrollmentResponse response = enrollmentService.enrollStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Student enrolled successfully", response));
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Batch enroll multiple students into a subject (Faculty only)")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> batchEnrollStudents(
            @Valid @RequestBody BatchEnrollmentRequest request) {
        log.info("REST request to batch enroll {} students in subject {}", 
                request.getStudentIds().size(), request.getSubjectId());
        List<EnrollmentResponse> response = enrollmentService.batchEnrollStudents(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Batch enrollment completed successfully", response));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('FACULTY') or (hasRole('STUDENT') and @securityUtils.isCurrentStudent(#studentId))")
    @Operation(summary = "Get enrollments for a student (Faculty or the student themselves)")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getEnrollmentsByStudent(
            @PathVariable Long studentId) {
        List<EnrollmentResponse> response = enrollmentService.getEnrollmentsByStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success("Student enrollments retrieved", response));
    }

    @GetMapping("/subject/{subjectId}")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Get list of enrolled students for a subject (Faculty only)")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getEnrolledStudentsBySubject(
            @PathVariable Long subjectId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) Integer semester) {
        List<EnrollmentResponse> response = enrollmentService.getEnrolledStudentsBySubject(subjectId, academicYear, semester);
        return ResponseEntity.ok(ApiResponse.success("Enrolled students retrieved", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Drop student enrollment (Faculty only)")
    public ResponseEntity<ApiResponse<Void>> dropEnrollment(@PathVariable Long id) {
        log.info("REST request to drop enrollment ID: {}", id);
        enrollmentService.dropEnrollment(id);
        return ResponseEntity.ok(ApiResponse.success("Enrollment dropped successfully", null));
    }
}
