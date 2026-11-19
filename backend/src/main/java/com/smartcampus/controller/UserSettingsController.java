package com.smartcampus.controller;

import com.smartcampus.dto.request.UserSettingsRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.UserSettingsResponse;
import com.smartcampus.security.SecurityUtils;
import com.smartcampus.service.UserSettingsService;
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

@RestController
@RequestMapping("/api/settings")
@Tag(name = "User Settings", description = "User Application and Notification Preferences APIs")
@SecurityRequirement(name = "BearerAuth")
public class UserSettingsController {

    private static final Logger log = LoggerFactory.getLogger(UserSettingsController.class);

    private final UserSettingsService userSettingsService;
    private final SecurityUtils securityUtils;

    @Autowired
    public UserSettingsController(UserSettingsService userSettingsService, SecurityUtils securityUtils) {
        this.userSettingsService = userSettingsService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get preferences and settings for current authenticated user")
    public ResponseEntity<ApiResponse<UserSettingsResponse>> getUserSettings() {
        Long userId = securityUtils.getCurrentUserId();
        log.info("Fetching settings for user ID: {}", userId);
        UserSettingsResponse response = userSettingsService.getUserSettings(userId);
        return ResponseEntity.ok(ApiResponse.success("User preferences retrieved", response));
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update theme and notification preferences for current user")
    public ResponseEntity<ApiResponse<UserSettingsResponse>> updateUserSettings(
            @Valid @RequestBody UserSettingsRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        log.info("Updating settings for user ID: {}", userId);
        UserSettingsResponse response = userSettingsService.updateUserSettings(userId, request);
        return ResponseEntity.ok(ApiResponse.success("User preferences updated successfully", response));
    }
}
