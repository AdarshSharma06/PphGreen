package com.pphgreen.backend.notification.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pphgreen.backend.common.response.ApiResponse;
import com.pphgreen.backend.notification.dto.NotificationResponse;
import com.pphgreen.backend.notification.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getMyNotifications(currentUserEmail())));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read",
                notificationService.markAsRead(id, currentUserEmail())));
    }

    private String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}