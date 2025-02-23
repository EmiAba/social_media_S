package app.web;

import app.notification.model.Notification;
import app.notification.service.NotificationService;
import app.user.model.User;
import jakarta.servlet.http.HttpSession;
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
        int unreadCount = notificationService.getUnreadCount(user);

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadNotificationCount", unreadCount);

        return "notifications";
    }

    @PostMapping("/{id}/read")
    @ResponseBody
    public int markAsRead(@PathVariable UUID id, HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null) {
            return 0;
        }

        notificationService.markAsRead(id);
        return notificationService.getUnreadCount(user);
    }

    @PostMapping("/read-all")
    @ResponseBody
    public int markAllAsRead(HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null) {
            return 0;
        }

        notificationService.markAllAsRead(user);
        return 0;
    }

    @GetMapping("/unread-count")
    @ResponseBody
    public int getUnreadCount(HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null) {
            return 0;
        }

        return notificationService.getUnreadCount(user);
    }

    @DeleteMapping("/{id}")
    public String deleteNotification(@PathVariable UUID id, HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        if (user != null) {
            notificationService.deleteNotification(id);
        }
        return "redirect:/notifications";
    }

}