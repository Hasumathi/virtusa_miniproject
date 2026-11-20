package com.smartcampus.controller;

import com.smartcampus.dto.request.SubjectCreateRequest;
import com.smartcampus.dto.request.SubjectUpdateRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.PagedResponse;
import com.smartcampus.dto.response.SubjectResponse;
import com.smartcampus.service.SubjectService;
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

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@Tag(name = "Subjects", description = "Subject Catalog Management APIs")
@SecurityRequirement(name = "BearerAuth")
public class SubjectController {

    private static final Logger log = LoggerFactory.getLogger(SubjectController.class);

    private final SubjectService subjectService;

    @Autowired
    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @PostMapping
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Create a new academic subject (Faculty only)")
    public ResponseEntity<ApiResponse<SubjectResponse>> createSubject(
            @Valid @RequestBody SubjectCreateRequest request) {
        log.info("REST request to create subject: {}", request.getSubjectCode());
        SubjectResponse response = subjectService.createSubject(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subject created successfully", response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get list of all subjects with pagination and department/semester filter")
    public ResponseEntity<ApiResponse<PagedResponse<SubjectResponse>>> getAllSubjects(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer semester,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PagedResponse<SubjectResponse> response = subjectService.getAllSubjects(
                departmentId, semester, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Subjects retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get subject details by ID")
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubjectById(@PathVariable Long id) {
        SubjectResponse response = subjectService.getSubjectById(id);
        return ResponseEntity.ok(ApiResponse.success("Subject retrieved successfully", response));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get subject details by subject code")
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubjectByCode(@PathVariable String code) {
        SubjectResponse response = subjectService.getSubjectByCode(code);
        return ResponseEntity.ok(ApiResponse.success("Subject retrieved successfully", response));
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get subjects offered by department")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getSubjectsByDepartment(
            @PathVariable Long departmentId) {
        List<SubjectResponse> response = subjectService.getSubjectsByDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success("Department subjects retrieved", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Update subject information (Faculty only)")
    public ResponseEntity<ApiResponse<SubjectResponse>> updateSubject(
            @PathVariable Long id,
            @Valid @RequestBody SubjectUpdateRequest request) {
        log.info("REST request to update subject ID: {}", id);
        SubjectResponse response = subjectService.updateSubject(id, request);
        return ResponseEntity.ok(ApiResponse.success("Subject updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Delete subject (Faculty only)")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(@PathVariable Long id) {
        log.info("REST request to delete subject ID: {}", id);
        subjectService.deleteSubject(id);
        return ResponseEntity.ok(ApiResponse.success("Subject deleted successfully", null));
    }
}
