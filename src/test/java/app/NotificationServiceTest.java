package app;

import app.notification.model.Notification;
import app.notification.model.NotificationType;
import app.notification.repository.NotificationRepository;
import app.notification.service.NotificationService;
import app.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Notification notification;
    private UUID notificationId;

    @BeforeEach
    void setUp() {
        notificationId = UUID.randomUUID();
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");

        notification = Notification.builder()
                .id(notificationId)
                .user(user)
                .type(NotificationType.MESSAGE)
                .message("Test notification")
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();
    }

    @Test
    void getUserNotifications() {
        List<Notification> notifications = Arrays.asList(notification);
        when(notificationRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(notifications);

        List<Notification> result = notificationService.getUserNotifications(user);

        assertEquals(1, result.size());
        verify(notificationRepository).findByUserOrderByCreatedAtDesc(user);
    }

    @Test
    void markAsRead() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(notificationId);

        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createMessageNotification() {
        String senderUsername = "sender";

        notificationService.createMessageNotification(user, senderUsername);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createLikeNotification() {
        String likerUsername = "liker";

        notificationService.createLikeNotification(user, likerUsername);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createFollowNotification() {
        String followerUsername = "follower";

        notificationService.createFollowNotification(user, followerUsername);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void getUnreadCount() {
        when(notificationRepository.countByUserAndIsReadFalse(user)).thenReturn(5);

        int count = notificationService.getUnreadCount(user);

        assertEquals(5, count);
        verify(notificationRepository).countByUserAndIsReadFalse(user);
    }

    @Test
    void markAllAsRead() {
        List<Notification> unreadNotifications = Arrays.asList(notification);
        when(notificationRepository.findByUserAndIsReadFalse(user)).thenReturn(unreadNotifications);

        notificationService.markAllAsRead(user);

        verify(notificationRepository).findByUserAndIsReadFalse(user);
        verify(notificationRepository).saveAll(unreadNotifications);
    }

    @Test
    void deleteNotification() {
        notificationService.deleteNotification(notificationId);

        verify(notificationRepository).deleteById(notificationId);
    }
}