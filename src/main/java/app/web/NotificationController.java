package app.web;

import app.exceprion.DomainException;
import app.notification.model.Notification;
import app.notification.service.NotificationService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView showNotifications(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getUserById(authenticationDetails.getUserId());

        if (user == null) {
            return new ModelAndView("redirect:/login");
        }

        List<Notification> notifications = notificationService.getUserNotifications(user);
        int unreadCount = notificationService.getUnreadCount(user);

        ModelAndView modelAndView = new ModelAndView("notifications");
        modelAndView.addObject("notifications", notifications);
        modelAndView.addObject("unreadNotificationCount", unreadCount);
        modelAndView.addObject("user", user);
        modelAndView.addObject("isAdmin", user.getRole() == UserRole.ADMIN);

        return modelAndView;
    }
    @PostMapping("/read-all")
    public String markAllAsRead(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getUserById(authenticationDetails.getUserId());
        if (user == null) {
            return "redirect:/login";
        }

        notificationService.markAllAsRead(user);
        return "redirect:/notifications";
    }

    @DeleteMapping("/{id}")
    public String deleteNotification(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getUserById(authenticationDetails.getUserId());
        if (user != null) {
            notificationService.deleteNotification(id);
        }
        return "redirect:/notifications";
    }
}