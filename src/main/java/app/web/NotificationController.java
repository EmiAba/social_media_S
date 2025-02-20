package app.web;

import app.notification.model.Notification;
import app.notification.service.NotificationService;
import app.user.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public String showNotifications(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null) {
            return "redirect:/login";
        }

        List<Notification> notifications = notificationService.getUserNotifications(user);


        model.addAttribute("notifications", notifications);
        return "notifications";
    }

    @PostMapping("/{id}/read")
    @ResponseBody
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id, HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    @ResponseBody
    public ResponseEntity<Void> markAllAsRead(HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        notificationService.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    @ResponseBody
    public ResponseEntity<Integer> getUnreadCount(HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        int count = notificationService.getUnreadCount(user);
        return ResponseEntity.ok(count);
    }
}