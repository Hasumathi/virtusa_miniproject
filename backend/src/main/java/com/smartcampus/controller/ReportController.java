package com.smartcampus.controller;

import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.StudentAttendanceSummaryResponse;
import com.smartcampus.entity.Student;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.security.SecurityUtils;
import com.smartcampus.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "PDF and Analytics Academic Report Generation APIs")
@SecurityRequirement(name = "BearerAuth")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    private final ReportService reportService;
    private final SecurityUtils securityUtils;
    private final StudentRepository studentRepository;

    @Autowired
    public ReportController(
            ReportService reportService,
            SecurityUtils securityUtils,
            StudentRepository studentRepository) {
        this.reportService = reportService;
        this.securityUtils = securityUtils;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/attendance/student/{studentId}")
    @PreAuthorize("hasRole('FACULTY') or (hasRole('STUDENT') and @securityUtils.isCurrentStudent(#studentId))")
    @Operation(summary = "Download individual student attendance report (PDF)")
    public ResponseEntity<byte[]> getStudentAttendanceReport(@PathVariable Long studentId) {
        log.info("Request for student attendance PDF report: ID={}", studentId);
        byte[] pdfBytes = reportService.generateStudentAttendancePdf(studentId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attendance_report_" + studentId + ".pdf\"")
                .body(pdfBytes);
    }

    @GetMapping("/attendance/class")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Download daily class attendance report (PDF) (Faculty only)")
    public ResponseEntity<byte[]> getClassAttendanceReport(
            @RequestParam Long subjectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Request for class attendance PDF: subjectId={}, date={}", subjectId, date);
        byte[] pdfBytes = reportService.generateClassAttendancePdf(subjectId, date);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"class_attendance_" + subjectId + "_" + date + ".pdf\"")
                .body(pdfBytes);
    }

    @GetMapping("/grades/subject")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Download subject-wise grade report (PDF) (Faculty only)")
    public ResponseEntity<byte[]> getSubjectGradeReport(
            @RequestParam Long subjectId,
            @RequestParam Integer semester,
            @RequestParam String academicYear) {
        log.info("Request for subject grades PDF: subjectId={}, sem={}, year={}", subjectId, semester, academicYear);
        byte[] pdfBytes = reportService.generateSubjectGradePdf(subjectId, semester, academicYear);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"grade_report_subject_" + subjectId + ".pdf\"")
                .body(pdfBytes);
    }

    @GetMapping("/grades/student/{studentId}")
    @PreAuthorize("hasRole('FACULTY') or (hasRole('STUDENT') and @securityUtils.isCurrentStudent(#studentId))")
    @Operation(summary = "Download student semester performance report (PDF)")
    public ResponseEntity<byte[]> getStudentPerformanceReport(
            @PathVariable Long studentId,
            @RequestParam Integer semester) {
        log.info("Request for student performance PDF: studentId={}, sem={}", studentId, semester);
        byte[] pdfBytes = reportService.generateStudentPerformancePdf(studentId, semester);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"performance_report_student_" + studentId + "_sem" + semester + ".pdf\"")
                .body(pdfBytes);
    }

    @GetMapping("/attendance/below-threshold")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Get list of students with attendance below threshold (Faculty only)")
    public ResponseEntity<ApiResponse<List<StudentAttendanceSummaryResponse>>> getStudentsBelowThreshold(
            @RequestParam(required = false) Long departmentId) {
        log.info("Request for students below threshold: departmentId={}", departmentId);
        List<StudentAttendanceSummaryResponse> list = reportService.getStudentsBelowThreshold(departmentId);
        return ResponseEntity.ok(ApiResponse.success("Students below attendance threshold retrieved", list));
    }

    @GetMapping("/my-attendance-pdf")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Download current student's personal attendance report (PDF)")
    public ResponseEntity<byte[]> getMyAttendanceReportPdf() {
        Long userId = securityUtils.getCurrentUserId();
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("No student profile found for current user"));

        byte[] pdfBytes = reportService.generateStudentAttendancePdf(student.getId());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"my_attendance_report.pdf\"")
                .body(pdfBytes);
    }

    @GetMapping("/my-performance-pdf")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Download current student's semester performance report (PDF)")
    public ResponseEntity<byte[]> getMyPerformanceReportPdf(@RequestParam(required = false) Integer semester) {
        Long userId = securityUtils.getCurrentUserId();
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("No student profile found for current user"));

        int targetSem = semester != null ? semester : student.getSemester();
        byte[] pdfBytes = reportService.generateStudentPerformancePdf(student.getId(), targetSem);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"my_performance_report_sem" + targetSem + ".pdf\"")
                .body(pdfBytes);
    }
}
