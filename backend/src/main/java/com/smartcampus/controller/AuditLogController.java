package com.smartcampus.controller;

import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.AuditLogResponse;
import com.smartcampus.dto.response.PagedResponse;
import com.smartcampus.entity.AuditLog;
import com.smartcampus.repository.AuditLogRepository;
import com.smartcampus.util.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@Tag(name = "Audit Logs", description = "System Audit and Administrative Activity Tracking APIs")
@SecurityRequirement(name = "BearerAuth")
public class AuditLogController {

    private static final Logger log = LoggerFactory.getLogger(AuditLogController.class);

    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Get paginated system audit logs (Faculty only)")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getAuditLogs(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Fetching audit logs: page={}, size={}", page, size);
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AuditLog> logPage = auditLogRepository.findAll(pageable);
        List<AuditLogResponse> content = logPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        PagedResponse<AuditLogResponse> response = PagedResponse.<AuditLogResponse>builder()
                .content(content)
                .pageNumber(logPage.getNumber())
                .pageSize(logPage.getSize())
                .totalElements(logPage.getTotalElements())
                .totalPages(logPage.getTotalPages())
                .first(logPage.isFirst())
                .last(logPage.isLast())
                .empty(logPage.isEmpty())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", response));
    }

    @GetMapping("/entity/{entityType}")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Get audit logs filtered by entity type (STUDENT, GRADE, ATTENDANCE, etc.)")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getAuditLogsByEntity(
            @PathVariable String entityType,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logPage = auditLogRepository.findByEntityTypeOrderByTimestampDesc(entityType.toUpperCase(), pageable);

        List<AuditLogResponse> content = logPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        PagedResponse<AuditLogResponse> response = PagedResponse.<AuditLogResponse>builder()
                .content(content)
                .pageNumber(logPage.getNumber())
                .pageSize(logPage.getSize())
                .totalElements(logPage.getTotalElements())
                .totalPages(logPage.getTotalPages())
                .first(logPage.isFirst())
                .last(logPage.isLast())
                .empty(logPage.isEmpty())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Entity audit logs retrieved", response));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Get recent audit activity for a specific user ID")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogsByUser(@PathVariable Long userId) {
        List<AuditLogResponse> list = auditLogRepository.findByUserIdOrderByTimestampDesc(userId).stream()
                .map(this::mapToResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("User audit activity retrieved", list));
    }

    private AuditLogResponse mapToResponse(AuditLog a) {
        return AuditLogResponse.builder()
                .id(a.getId())
                .userId(a.getUserId())
                .action(a.getAction())
                .entityType(a.getEntityType())
                .entityId(a.getEntityId())
                .description(a.getDescription())
                .timestamp(a.getTimestamp())
                .build();
    }
}
