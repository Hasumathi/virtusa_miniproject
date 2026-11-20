package com.smartcampus.controller;

import com.smartcampus.dto.request.FacultyUpdateRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.FacultyResponse;
import com.smartcampus.security.SecurityUtils;
import com.smartcampus.service.FacultyService;
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
@RequestMapping("/api/faculty")
@Tag(name = "Faculty", description = "Faculty Management APIs")
@SecurityRequirement(name = "BearerAuth")
public class FacultyController {

    private static final Logger log = LoggerFactory.getLogger(FacultyController.class);

    private final FacultyService facultyService;
    private final SecurityUtils securityUtils;

    @Autowired
    public FacultyController(FacultyService facultyService, SecurityUtils securityUtils) {
        this.facultyService = facultyService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Get currently authenticated faculty profile")
    public ResponseEntity<ApiResponse<FacultyResponse>> getMyProfile() {
        Long userId = securityUtils.getCurrentUserId();
        log.info("Fetching faculty profile for user ID: {}", userId);
        FacultyResponse response = facultyService.getFacultyProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Faculty profile retrieved", response));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Update permitted profile details for authenticated faculty")
    public ResponseEntity<ApiResponse<FacultyResponse>> updateMyProfile(
            @Valid @RequestBody FacultyUpdateRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        log.info("Updating faculty profile for user ID: {}", userId);
        FacultyResponse response = facultyService.updateFacultyProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Faculty profile updated successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get faculty details by ID")
    public ResponseEntity<ApiResponse<FacultyResponse>> getFacultyById(@PathVariable Long id) {
        FacultyResponse response = facultyService.getFacultyById(id);
        return ResponseEntity.ok(ApiResponse.success("Faculty details retrieved", response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all faculty members with optional department filter")
    public ResponseEntity<ApiResponse<List<FacultyResponse>>> getAllFaculty(
            @RequestParam(required = false) Long departmentId) {
        List<FacultyResponse> response = facultyService.getAllFaculty(departmentId);
        return ResponseEntity.ok(ApiResponse.success("Faculty members retrieved", response));
    }
}
