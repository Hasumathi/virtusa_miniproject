package com.smartcampus.service;

public interface AuditService {
    void logAction(Long userId, String action, String entityType, Long entityId, String description);
}
