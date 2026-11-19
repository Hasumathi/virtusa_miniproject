package com.smartcampus.controller;

import com.smartcampus.dto.request.AttendanceUpdateRequest;
import com.smartcampus.dto.request.BatchAttendanceRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.AttendanceResponse;
import com.smartcampus.dto.response.ClassAttendanceSummaryResponse;
import com.smartcampus.dto.response.PagedResponse;
import com.smartcampus.dto.response.StudentAttendanceSummaryResponse;
import com.smartcampus.entity.Student;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.security.SecurityUtils;
import com.smartcampus.service.AttendanceService;
import com.smartcampus.util.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@Tag(name = "Attendance", description = "Attendance Management APIs for Faculty and Students")
@SecurityRequirement(name = "BearerAuth")
public class AttendanceController {

    private static final Logger log = LoggerFactory.getLogger(AttendanceController.class);

    private final AttendanceService attendanceService;
    private final SecurityUtils securityUtils;
    private final StudentRepository studentRepository;

    @Autowired
    public AttendanceController(
            AttendanceService attendanceService,
            SecurityUtils securityUtils,
            StudentRepository studentRepository) {
        this.attendanceService = attendanceService;
        this.securityUtils = securityUtils;
        this.studentRepository = studentRepository;
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Mark attendance for enrolled students on a specific date (Faculty only)")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> markBatchAttendance(
            @Valid @RequestBody BatchAttendanceRequest request) {
        Long facultyUserId = securityUtils.getCurrentUserId();
        log.info("Faculty user ID {} marking attendance for subject {}", facultyUserId, request.getSubjectId());
        List<AttendanceResponse> response = attendanceService.markBatchAttendance(facultyUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attendance marked successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Update an individual attendance record (Faculty only)")
    public ResponseEntity<ApiResponse<AttendanceResponse>> updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceUpdateRequest request) {
        log.info("Updating attendance record ID: {}", id);
        AttendanceResponse response = attendanceService.updateAttendance(id, request);
        return ResponseEntity.ok(ApiResponse.success("Attendance updated successfully", response));
    }

    @GetMapping("/subject/{subjectId}")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Get attendance records for a subject on a specified date (Faculty only)")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendanceBySubjectAndDate(
            @PathVariable Long subjectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AttendanceResponse> response = attendanceService.getAttendanceBySubjectAndDate(subjectId, date);
        return ResponseEntity.ok(ApiResponse.success("Attendance records retrieved", response));
    }

    @GetMapping("/summary/class")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Get daily class attendance summary for a subject (Faculty only)")
    public ResponseEntity<ApiResponse<ClassAttendanceSummaryResponse>> getClassAttendanceSummary(
            @RequestParam Long subjectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        ClassAttendanceSummaryResponse response = attendanceService.getClassAttendanceSummary(subjectId, date);
        return ResponseEntity.ok(ApiResponse.success("Class attendance summary retrieved", response));
    }

    @GetMapping("/student/{studentId}/summary")
    @PreAuthorize("hasRole('FACULTY') or (hasRole('STUDENT') and @securityUtils.isCurrentStudent(#studentId))")
    @Operation(summary = "Get overall and subject-wise attendance summary for a student (Faculty or student themselves)")
    public ResponseEntity<ApiResponse<StudentAttendanceSummaryResponse>> getStudentAttendanceSummary(
            @PathVariable Long studentId) {
        StudentAttendanceSummaryResponse response = attendanceService.getStudentAttendanceSummary(studentId);
        return ResponseEntity.ok(ApiResponse.success("Student attendance summary retrieved", response));
    }

    @GetMapping("/student/{studentId}/history")
    @PreAuthorize("hasRole('FACULTY') or (hasRole('STUDENT') and @securityUtils.isCurrentStudent(#studentId))")
    @Operation(summary = "Get paginated attendance history for a student")
    public ResponseEntity<ApiResponse<PagedResponse<AttendanceResponse>>> getStudentAttendanceHistory(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        PagedResponse<AttendanceResponse> response = attendanceService.getStudentAttendanceHistory(studentId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Student attendance history retrieved", response));
    }

    @GetMapping("/my-summary")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Current student view of their own attendance summary")
    public ResponseEntity<ApiResponse<StudentAttendanceSummaryResponse>> getMyAttendanceSummary() {
        Long userId = securityUtils.getCurrentUserId();
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("No student profile linked to authenticated user"));
        StudentAttendanceSummaryResponse response = attendanceService.getStudentAttendanceSummary(student.getId());
        return ResponseEntity.ok(ApiResponse.success("Your attendance summary", response));
    }
}
