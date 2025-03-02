package app.notification.service;

import app.notification.model.Notification;
import app.notification.model.NotificationType;
import app.notification.repository.NotificationRepository;
import app.user.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public void markAsRead(UUID id) {
        notificationRepository.findById(id).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    public void createMessageNotification(User recipient, String senderUsername) {
        createNotification(recipient, NotificationType.MESSAGE, senderUsername + " sent you a new message");
    }

    public void createLikeNotification(User recipient, String likerUsername) {
        createNotification(recipient, NotificationType.LIKE, likerUsername + " liked your post");
    }

    public void createFollowNotification(User recipient, String followerUsername) {
        createNotification(recipient, NotificationType.FOLLOW, followerUsername + " started following you");
    }



    private void createNotification(User user, NotificationType type, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .createdAt(LocalDateTime.now())
                .message(message)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    public int getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalse(user);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    public void deleteNotification(UUID notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}

