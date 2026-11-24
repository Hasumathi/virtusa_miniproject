package com.smartcampus.controller;

import com.smartcampus.dto.request.*;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.AuthResponse;
import com.smartcampus.security.SecurityUtils;
import com.smartcampus.service.AuthService;
import com.smartcampus.util.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user login, JWT generation, password management, and account activation")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final SecurityUtils securityUtils;

    @Autowired
    public AuthController(AuthService authService, SecurityUtils securityUtils) {
        this.authService = authService;
        this.securityUtils = securityUtils;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user (Faculty or Student) and obtain JWT access token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("REST request to login user: {}", request.getUsernameOrEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", response));
    }

    @PostMapping("/faculty/register")
    @Operation(summary = "Register authorized faculty account using administrative secret key")
    public ResponseEntity<ApiResponse<AuthResponse>> registerFaculty(
            @Valid @RequestBody FacultyRegisterRequest request) {
        log.info("REST request to register faculty: {}", request.getEmployeeId());
        AuthResponse response = authService.registerFaculty(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Faculty account registered successfully", response));
    }

    @PostMapping("/student/register")
    @Operation(summary = "Register a new student account")
    public ResponseEntity<ApiResponse<AuthResponse>> registerStudent(
            @Valid @RequestBody StudentRegisterRequest request) {
        log.info("REST request to register student: {}", request.getRegisterNumber());
        AuthResponse response = authService.registerStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Student account registered successfully", response));
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change password for the currently authenticated user")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        Long currentUserId = securityUtils.getCurrentUserId();
        log.info("REST request to change password for user ID: {}", currentUserId);
        authService.changePassword(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset token by email")
    public ResponseEntity<ApiResponse<Map<String, String>>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        log.info("REST request for forgot password token: {}", request.getEmail());
        String resetToken = authService.forgotPassword(request);
        Map<String, String> data = new HashMap<>();
        data.put("resetToken", resetToken);
        data.put("instructions", "Use this token within 15 minutes to reset your password via /api/auth/reset-password");
        return ResponseEntity.ok(ApiResponse.success("Password reset token generated", data));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using a valid reset token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        log.info("REST request to reset password with token");
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password has been reset successfully", null));
    }

    @PostMapping("/activate")
    @Operation(summary = "Activate student account and set initial password")
    public ResponseEntity<ApiResponse<Void>> activateAccount(
            @Valid @RequestBody AccountActivationRequest request) {
        log.info("REST request to activate account for identifier: {}", request.getIdentifier());
        authService.activateAccount(request);
        return ResponseEntity.ok(ApiResponse.success("Account activated successfully. You can now login.", null));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user and invalidate JWT access token")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = AppConstants.HEADER_AUTHORIZATION, required = false) String authHeader) {
        log.info("REST request to logout user");
        authService.logout(authHeader);
        return ResponseEntity.ok(ApiResponse.success("User logged out successfully", null));
    }
}
