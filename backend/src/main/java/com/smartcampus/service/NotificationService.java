package com.smartcampus.service;

import com.smartcampus.dto.response.NotificationResponse;
import com.smartcampus.dto.response.PagedResponse;
import com.smartcampus.entity.User;
import com.smartcampus.enums.NotificationType;

public interface NotificationService {

    void sendNotification(User user, String title, String message, NotificationType type);

    PagedResponse<NotificationResponse> getUserNotifications(Long userId, int page, int size);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);

    long getUnreadCount(Long userId);
}
