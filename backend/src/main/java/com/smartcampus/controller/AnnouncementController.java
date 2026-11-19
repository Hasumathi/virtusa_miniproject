package com.smartcampus.controller;

import com.smartcampus.dto.request.AnnouncementCreateRequest;
import com.smartcampus.dto.response.AnnouncementResponse;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.PagedResponse;
import com.smartcampus.security.SecurityUtils;
import com.smartcampus.service.AnnouncementService;
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
@RequestMapping("/api/announcements")
@Tag(name = "Announcements", description = "Campus and Departmental Announcement APIs")
@SecurityRequirement(name = "BearerAuth")
public class AnnouncementController {

    private static final Logger log = LoggerFactory.getLogger(AnnouncementController.class);

    private final AnnouncementService announcementService;
    private final SecurityUtils securityUtils;

    @Autowired
    public AnnouncementController(AnnouncementService announcementService, SecurityUtils securityUtils) {
        this.announcementService = announcementService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Publish a new announcement (Faculty only)")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> createAnnouncement(
            @Valid @RequestBody AnnouncementCreateRequest request) {
        Long facultyUserId = securityUtils.getCurrentUserId();
        log.info("Faculty user ID {} creating announcement: {}", facultyUserId, request.getTitle());
        AnnouncementResponse response = announcementService.createAnnouncement(facultyUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Announcement published successfully", response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Retrieve announcements with department, year, and semester filter")
    public ResponseEntity<ApiResponse<PagedResponse<AnnouncementResponse>>> getAnnouncements(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer semester,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        PagedResponse<AnnouncementResponse> response = announcementService.getAnnouncements(
                departmentId, year, semester, page, size);
        return ResponseEntity.ok(ApiResponse.success("Announcements retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get announcement by ID")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> getAnnouncementById(@PathVariable Long id) {
        AnnouncementResponse response = announcementService.getAnnouncementById(id);
        return ResponseEntity.ok(ApiResponse.success("Announcement details retrieved", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Delete an announcement (Author faculty only)")
    public ResponseEntity<ApiResponse<Void>> deleteAnnouncement(@PathVariable Long id) {
        Long facultyUserId = securityUtils.getCurrentUserId();
        announcementService.deleteAnnouncement(id, facultyUserId);
        return ResponseEntity.ok(ApiResponse.success("Announcement deleted successfully", null));
    }
}
