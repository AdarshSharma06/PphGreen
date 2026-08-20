package com.pphgreen.backend.notification.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.pphgreen.backend.common.exception.ForbiddenException;
import com.pphgreen.backend.common.exception.ResourceNotFoundException;
import com.pphgreen.backend.common.exception.UnauthorizedException;
import com.pphgreen.backend.notification.dto.NotificationResponse;
import com.pphgreen.backend.notification.entity.Notification;
import com.pphgreen.backend.notification.repository.NotificationRepository;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.service.UserService;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public NotificationService(NotificationRepository notificationRepository, UserService userService) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
    }

    public List<NotificationResponse> getMyNotifications(String email) {
        User user = currentUser(email);
        return notificationRepository.findAllWithRecipientByRecipientId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public NotificationResponse markAsRead(Long id, String email) {
        User user = currentUser(email);
        Notification notification = notificationRepository.findWithRecipientById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        if (!Objects.equals(user.getEmail(), notification.getRecipient().getEmail())) {
            throw new ForbiddenException("You can only mark your own notifications as read");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
        return toResponse(notification);
    }

    private User currentUser(String email) {
        return userService.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getType(), notification.getTitle(),
                notification.getMessage(), notification.isRead(), notification.getCreatedAt());
    }
}