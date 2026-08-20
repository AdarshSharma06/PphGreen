package com.pphgreen.backend.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pphgreen.backend.common.exception.ForbiddenException;
import com.pphgreen.backend.common.exception.ResourceNotFoundException;
import com.pphgreen.backend.common.exception.UnauthorizedException;
import com.pphgreen.backend.notification.dto.NotificationResponse;
import com.pphgreen.backend.notification.entity.Notification;
import com.pphgreen.backend.notification.entity.NotificationType;
import com.pphgreen.backend.notification.repository.NotificationRepository;
import com.pphgreen.backend.user.entity.User;
import com.pphgreen.backend.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserService userService;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, userService);
    }

    @Test
    void getNotificationsReturnsOnlyCurrentUsersNotifications() {
        User user = user("member@example.com");
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(user));
        when(notificationRepository.findAllWithRecipientByRecipientId(any()))
                .thenReturn(List.of(notification(1L, user, "Welcome"), notification(2L, user, "Event reminder")));

        List<NotificationResponse> responses = notificationService.getMyNotifications("member@example.com");

        assertEquals(2, responses.size());
        assertEquals("Welcome", responses.get(0).title());
        verify(notificationRepository).findAllWithRecipientByRecipientId(any());
    }

    @Test
    void notificationsReturnedNewestFirst() {
        User user = user("member@example.com");
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(user));
        Notification newest = notification(2L, user, "Newer");
        Notification oldest = notification(1L, user, "Older");
        when(notificationRepository.findAllWithRecipientByRecipientId(any())).thenReturn(List.of(newest, oldest));

        List<NotificationResponse> responses = notificationService.getMyNotifications("member@example.com");

        assertEquals("Newer", responses.get(0).title());
        assertEquals("Older", responses.get(1).title());
    }

    @Test
    void memberCanReadOwnNotifications() {
        User member = user("member@example.com");
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(notificationRepository.findAllWithRecipientByRecipientId(any()))
                .thenReturn(List.of(notification(1L, member, "Member notification")));

        List<NotificationResponse> responses = notificationService.getMyNotifications("member@example.com");

        assertEquals(1, responses.size());
        assertEquals("Member notification", responses.get(0).title());
    }

    @Test
    void adminCanReadOwnNotifications() {
        User admin = user("admin@example.com");
        when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(notificationRepository.findAllWithRecipientByRecipientId(any()))
                .thenReturn(List.of(notification(1L, admin, "Admin notification")));

        List<NotificationResponse> responses = notificationService.getMyNotifications("admin@example.com");

        assertEquals(1, responses.size());
        assertEquals("Admin notification", responses.get(0).title());
    }

    @Test
    void markOwnNotificationAsRead() {
        User user = user("member@example.com");
        Notification notification = notification(1L, user, "Welcome");
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(user));
        when(notificationRepository.findWithRecipientById(1L)).thenReturn(Optional.of(notification));

        NotificationResponse response = notificationService.markAsRead(1L, "member@example.com");

        assertTrue(notification.isRead());
        assertTrue(response.read());
        verify(notificationRepository).save(notification);
    }

    @Test
    void cannotMarkAnotherUsersNotification() {
        User user = user("member@example.com");
        User other = user("other@example.com");
        Notification notification = notification(1L, other, "Private notification");
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(user));
        when(notificationRepository.findWithRecipientById(1L)).thenReturn(Optional.of(notification));

        assertThrows(ForbiddenException.class, () -> notificationService.markAsRead(1L, "member@example.com"));
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void missingNotificationThrowsResourceNotFound() {
        User user = user("member@example.com");
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(user));
        when(notificationRepository.findWithRecipientById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> notificationService.markAsRead(999L, "member@example.com"));

        assertEquals("Notification not found with id: 999", ex.getMessage());
    }

    @Test
    void markingAlreadyReadNotificationIsSafe() {
        User user = user("member@example.com");
        Notification notification = notification(1L, user, "Welcome");
        notification.setRead(true);
        when(userService.findByEmail("member@example.com")).thenReturn(Optional.of(user));
        when(notificationRepository.findWithRecipientById(1L)).thenReturn(Optional.of(notification));

        NotificationResponse response = notificationService.markAsRead(1L, "member@example.com");

        assertTrue(notification.isRead());
        assertTrue(response.read());
        verify(notificationRepository).save(notification);
    }

    @Test
    void getNotificationsWithUnknownUserThrowsUnauthorized() {
        when(userService.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class,
                () -> notificationService.getMyNotifications("ghost@example.com"));
        verify(notificationRepository, never()).findAllWithRecipientByRecipientId(any());
    }

    @Test
    void markAsReadWithUnknownUserThrowsUnauthorized() {
        when(userService.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class,
                () -> notificationService.markAsRead(1L, "ghost@example.com"));
        verify(notificationRepository, never()).findWithRecipientById(any());
    }

    private User user(String email) {
        User user = new User();
        user.setEmail(email);
        return user;
    }

    private Notification notification(Long id, User recipient, String title) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setRecipient(recipient);
        notification.setType(NotificationType.EVENT);
        notification.setTitle(title);
        notification.setMessage("Message: " + title);
        notification.setRead(false);
        return notification;
    }
}