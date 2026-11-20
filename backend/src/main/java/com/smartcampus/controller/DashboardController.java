package com.smartcampus.controller;

import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.FacultyDashboardResponse;
import com.smartcampus.dto.response.StudentDashboardResponse;
import com.smartcampus.security.SecurityUtils;
import com.smartcampus.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Aggregated Analytics and Metrics APIs")
@SecurityRequirement(name = "BearerAuth")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;
    private final SecurityUtils securityUtils;

    @Autowired
    public DashboardController(DashboardService dashboardService, SecurityUtils securityUtils) {
        this.dashboardService = dashboardService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/faculty")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Get high-level analytics for Faculty dashboard (Faculty only)")
    public ResponseEntity<ApiResponse<FacultyDashboardResponse>> getFacultyDashboard() {
        log.info("Fetching faculty dashboard data");
        FacultyDashboardResponse response = dashboardService.getFacultyDashboard();
        return ResponseEntity.ok(ApiResponse.success("Faculty dashboard metrics retrieved", response));
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get personalized metrics for authenticated Student dashboard (Student only)")
    public ResponseEntity<ApiResponse<StudentDashboardResponse>> getStudentDashboard() {
        Long userId = securityUtils.getCurrentUserId();
        log.info("Fetching student dashboard data for user ID: {}", userId);
        StudentDashboardResponse response = dashboardService.getStudentDashboard(userId);
        return ResponseEntity.ok(ApiResponse.success("Student dashboard metrics retrieved", response));
    }
}
