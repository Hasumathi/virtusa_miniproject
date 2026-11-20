package com.smartcampus.controller;

import com.smartcampus.dto.request.GradeEntryRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.GradeResponse;
import com.smartcampus.dto.response.SemesterGradeSummaryResponse;
import com.smartcampus.entity.Student;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.security.SecurityUtils;
import com.smartcampus.service.GradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
@Tag(name = "Grades", description = "Grade and Marks Management APIs")
@SecurityRequirement(name = "BearerAuth")
public class GradeController {

    private static final Logger log = LoggerFactory.getLogger(GradeController.class);

    private final GradeService gradeService;
    private final SecurityUtils securityUtils;
    private final StudentRepository studentRepository;

    @Autowired
    public GradeController(
            GradeService gradeService,
            SecurityUtils securityUtils,
            StudentRepository studentRepository) {
        this.gradeService = gradeService;
        this.securityUtils = securityUtils;
        this.studentRepository = studentRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Enter or update student marks for a subject (Faculty only)")
    public ResponseEntity<ApiResponse<GradeResponse>> enterOrUpdateGrade(
            @Valid @RequestBody GradeEntryRequest request) {
        log.info("Entering/updating marks for student: {} in subject: {}", request.getStudentId(), request.getSubjectId());
        GradeResponse response = gradeService.enterOrUpdateGrade(request);
        return ResponseEntity.ok(ApiResponse.success("Marks saved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Get grade details by ID (Faculty only)")
    public ResponseEntity<ApiResponse<GradeResponse>> getGradeById(@PathVariable Long id) {
        GradeResponse response = gradeService.getGradeById(id);
        return ResponseEntity.ok(ApiResponse.success("Grade record retrieved", response));
    }

    @GetMapping("/subject/{subjectId}")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Get all student grades for a subject and semester (Faculty only)")
    public ResponseEntity<ApiResponse<List<GradeResponse>>> getGradesBySubjectAndSemester(
            @PathVariable Long subjectId,
            @RequestParam Integer semester) {
        List<GradeResponse> response = gradeService.getGradesBySubjectAndSemester(subjectId, semester);
        return ResponseEntity.ok(ApiResponse.success("Subject grades retrieved", response));
    }

    @PostMapping("/publish")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Publish results for a subject, semester, and academic year (Faculty only)")
    public ResponseEntity<ApiResponse<Void>> publishGrades(
            @RequestParam Long subjectId,
            @RequestParam Integer semester,
            @RequestParam String academicYear) {
        log.info("Publishing results for subject: {}, sem: {}, year: {}", subjectId, semester, academicYear);
        gradeService.publishGrades(subjectId, semester, academicYear);
        return ResponseEntity.ok(ApiResponse.success("Results published successfully. Students can now view their grades.", null));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('FACULTY') or (hasRole('STUDENT') and @securityUtils.isCurrentStudent(#studentId))")
    @Operation(summary = "Get all published grades for a student (Faculty or student themselves)")
    public ResponseEntity<ApiResponse<List<GradeResponse>>> getStudentGrades(
            @PathVariable Long studentId) {
        List<GradeResponse> response = gradeService.getStudentGrades(studentId);
        return ResponseEntity.ok(ApiResponse.success("Student grades retrieved", response));
    }

    @GetMapping("/student/{studentId}/semester/{semester}")
    @PreAuthorize("hasRole('FACULTY') or (hasRole('STUDENT') and @securityUtils.isCurrentStudent(#studentId))")
    @Operation(summary = "Get student semester performance summary and grades (Faculty or student themselves)")
    public ResponseEntity<ApiResponse<SemesterGradeSummaryResponse>> getStudentSemesterGrades(
            @PathVariable Long studentId,
            @PathVariable Integer semester) {
        SemesterGradeSummaryResponse response = gradeService.getStudentSemesterGrades(studentId, semester);
        return ResponseEntity.ok(ApiResponse.success("Semester performance retrieved", response));
    }

    @GetMapping("/my-grades")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Current student view of their own published grades")
    public ResponseEntity<ApiResponse<List<GradeResponse>>> getMyGrades() {
        Long userId = securityUtils.getCurrentUserId();
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("No student profile found for current user"));
        List<GradeResponse> response = gradeService.getStudentGrades(student.getId());
        return ResponseEntity.ok(ApiResponse.success("Your published grades", response));
    }
}
