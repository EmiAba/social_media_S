package app.web;

import app.follow.service.FollowService;
import app.message.model.Message;
import app.message.service.MessageService;
import app.notification.service.NotificationService;
import app.security.AuthenticationDetails;
import app.user.service.UserService;
import app.web.dto.MessageRequest;
import app.user.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;
    private final FollowService followService;
    private final NotificationService notificationService;

    @Autowired
    public MessageController(
            MessageService messageService,
            UserService userService,
            FollowService followService,
            NotificationService notificationService) {
        this.messageService = messageService;
        this.userService = userService;
        this.followService = followService;
        this.notificationService = notificationService;
    }

    @GetMapping("/inbox")
    public ModelAndView getInbox(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getById(authenticationDetails.getUserId());
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }
        List<Message> messages = messageService.getReceivedMessagesAndMarkAsRead(user.getId());
        ModelAndView modelAndView = new ModelAndView("inbox");
        modelAndView.addObject("messages", messages);
        modelAndView.addObject("user", user);  // Add the entire user object to match home.html

        return modelAndView;
    }

    @GetMapping("/send_message")
    public ModelAndView showSendMessagePage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getById(authenticationDetails.getUserId());
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }
        ModelAndView modelAndView = new ModelAndView("send_message");
        modelAndView.addObject("messageRequest", new MessageRequest());
        modelAndView.addObject("user", user);  // Add the entire user object to match home.html

        return modelAndView;
    }

    @GetMapping("/inbox/{userId}")
    public ModelAndView showDirectMessage(@PathVariable UUID userId, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User currentUser = userService.getById(authenticationDetails.getUserId());
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }

        Optional<User> recipientOpt = userService.getUserById(userId);
        if (recipientOpt.isEmpty()) {
            return new ModelAndView("redirect:/home");
        }

        User recipient = recipientOpt.get();
        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setUsername(recipient.getUsername());

        ModelAndView modelAndView = new ModelAndView("send_message");
        modelAndView.addObject("messageRequest", messageRequest);
        modelAndView.addObject("recipient", recipient);
        modelAndView.addObject("user", currentUser);  // Add the entire user object to match home.html

        return modelAndView;
    }

    @PostMapping("/send")
    public ModelAndView sendMessage(
            @ModelAttribute("messageRequest") MessageRequest messageRequest,
            BindingResult bindingResult,
            @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("send_message");
            User user = userService.getById(authenticationDetails.getUserId());
            if (user != null) {
                modelAndView.addObject("user", user);
            }
            return modelAndView;
        }

        User sender = userService.getById(authenticationDetails.getUserId());
        if (sender == null) {
            return new ModelAndView("redirect:/login");
        }

        Optional<User> recipientOpt = userService.getUserByUsername(messageRequest.getUsername());
        if (recipientOpt.isPresent()) {
            User recipient = recipientOpt.get();

            messageService.saveMessage(messageRequest.getUsername(), messageRequest.getContent(), sender);

            System.out.println("Creating notification for message from " + sender.getUsername() + " to " + recipient.getUsername());
            notificationService.createMessageNotification(recipient, sender.getUsername());
        }

        return new ModelAndView("redirect:/inbox");
    }

    @DeleteMapping("/inbox/delete/{messageId}")
    public ModelAndView deleteMessage(@PathVariable UUID messageId, @RequestParam String content, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getById(authenticationDetails.getUserId());
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }

        messageService.deleteMessage(messageId, user.getId());

        return new ModelAndView("redirect:/inbox");
    }
}