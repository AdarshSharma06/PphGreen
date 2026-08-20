package com.pphgreen.backend.notification.dto;

import java.time.Instant;

import com.pphgreen.backend.notification.entity.NotificationType;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String message,
        boolean read,
        Instant createdAt) {
}