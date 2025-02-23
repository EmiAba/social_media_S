package app.notification.service;

import app.notification.model.Notification;
import app.notification.model.NotificationType;
import app.notification.repository.NotificationRepository;
import app.user.model.User;
import org.springframework.stereotype.Service;

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
        createNotification(recipient, NotificationType.MESSAGE, senderUsername);
    }

    public void createLikeNotification(User recipient, String likerUsername) {
        createNotification(recipient, NotificationType.LIKE, likerUsername);
    }

    public void createFollowNotification(User recipient, String followerUsername) {
        createNotification(recipient, NotificationType.FOLLOW, followerUsername);
    }

    public void createOnlineStatusNotification(User recipient, String username, boolean isOnline) {
        NotificationType type = isOnline ? NotificationType.USER_ONLINE : NotificationType.USER_OFFLINE;
        createNotification(recipient, type, username);
    }

    public void createCommentNotification(User recipient, String commenterUsername) {
        createNotification(recipient, NotificationType.COMMENT, commenterUsername);
    }

    private void createNotification(User user, NotificationType type, String username) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .message(type.formatMessage(username))
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