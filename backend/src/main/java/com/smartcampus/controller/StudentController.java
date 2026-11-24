package com.smartcampus.controller;

import com.smartcampus.dto.request.StudentCreateRequest;
import com.smartcampus.dto.request.StudentUpdateRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.PagedResponse;
import com.smartcampus.dto.response.StudentResponse;
import com.smartcampus.enums.AccountStatus;
import com.smartcampus.service.StudentService;
import com.smartcampus.util.AppConstants;
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

@RestController
@RequestMapping("/api/students")
@Tag(name = "Students", description = "Student Management Module APIs")
@SecurityRequirement(name = "BearerAuth")
public class StudentController {

    private static final Logger log = LoggerFactory.getLogger(StudentController.class);

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Create a new student record (Faculty only)")
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(
            @Valid @RequestBody StudentCreateRequest request) {
        log.info("REST request to create student: {}", request.getRegisterNumber());
        StudentResponse response = studentService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Student record created successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Retrieve all students with pagination, search, and filters (Faculty only)")
    public ResponseEntity<ApiResponse<PagedResponse<StudentResponse>>> getAllStudents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDir) {

        log.debug("REST request to get students with search='{}', dept={}, year={}, sem={}", 
                search, departmentId, year, semester);
        PagedResponse<StudentResponse> response = studentService.getAllStudents(
                search, departmentId, year, semester, section, status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Students retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('FACULTY') or (hasRole('STUDENT') and @securityUtils.isCurrentStudent(#id))")
    @Operation(summary = "Get student details by ID (Faculty or the authenticated Student themselves)")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentById(@PathVariable Long id) {
        log.debug("REST request to get student by ID: {}", id);
        StudentResponse response = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.success("Student retrieved successfully", response));
    }

    @GetMapping("/reg/{registerNumber}")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Get student details by Register Number (Faculty only)")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentByRegisterNumber(
            @PathVariable String registerNumber) {
        log.debug("REST request to get student by register number: {}", registerNumber);
        StudentResponse response = studentService.getStudentByRegisterNumber(registerNumber);
        return ResponseEntity.ok(ApiResponse.success("Student retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Update an existing student record (Faculty only)")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentUpdateRequest request) {
        log.info("REST request to update student ID: {}", id);
        StudentResponse response = studentService.updateStudent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Student record updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Deactivate and soft-delete student record (Faculty only)")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable Long id) {
        log.info("REST request to soft-delete student ID: {}", id);
        studentService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.success("Student record deactivated successfully", null));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Change student account status (Faculty only)")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudentStatus(
            @PathVariable Long id,
            @RequestParam AccountStatus status) {
        log.info("REST request to update student ID: {} status to {}", id, status);
        StudentResponse response = studentService.updateStudentStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Student status updated successfully", response));
    }
}
