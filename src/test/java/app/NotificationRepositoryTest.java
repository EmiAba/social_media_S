package app;



import app.notification.model.Notification;
import app.notification.model.NotificationType;
import app.notification.repository.NotificationRepository;
import app.user.model.User;
import app.user.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationRepository notificationRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .role(UserRole.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persist(user);

        Notification notification1 = Notification.builder()
                .user(user)
                .type(NotificationType.MESSAGE)
                .message("Test notification 1")
                .createdAt(LocalDateTime.now().minusHours(1))
                .isRead(false)
                .build();

        Notification notification2 = Notification.builder()
                .user(user)
                .type(NotificationType.LIKE)
                .message("Test notification 2")
                .createdAt(LocalDateTime.now().minusHours(2))
                .isRead(true)
                .build();

        Notification notification3 = Notification.builder()
                .user(user)
                .type(NotificationType.FOLLOW)
                .message("Test notification 3")
                .createdAt(LocalDateTime.now().minusHours(3))
                .isRead(false)
                .build();

        entityManager.persist(notification1);
        entityManager.persist(notification2);
        entityManager.persist(notification3);
        entityManager.flush();
    }

    @Test
    void findByUserOrderByCreatedAtDesc() {
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);

        assertEquals(3, notifications.size());
        assertEquals("Test notification 1", notifications.get(0).getMessage());
        assertEquals("Test notification 2", notifications.get(1).getMessage());
        assertEquals("Test notification 3", notifications.get(2).getMessage());
    }

    @Test
    void findByUserAndIsReadFalse() {
        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalse(user);

        assertEquals(2, unreadNotifications.size());
    }

    @Test
    void countByUserAndIsReadFalse() {
        int count = notificationRepository.countByUserAndIsReadFalse(user);

        assertEquals(2, count);
    }

    @Test
    void multipleUsers() {
        User user2 = User.builder()
                .username("anotheruser")
                .password("password")
                .email("another@example.com")
                .role(UserRole.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persist(user2);

        Notification notification4 = Notification.builder()
                .user(user2)
                .type(NotificationType.MESSAGE)
                .message("User2 notification")
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();
        entityManager.persist(notification4);
        entityManager.flush();

        List<Notification> user1Notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        List<Notification> user2Notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user2);
        int user1UnreadCount = notificationRepository.countByUserAndIsReadFalse(user);
        int user2UnreadCount = notificationRepository.countByUserAndIsReadFalse(user2);

        assertEquals(3, user1Notifications.size());
        assertEquals(1, user2Notifications.size());
        assertEquals(2, user1UnreadCount);
        assertEquals(1, user2UnreadCount);
    }
}