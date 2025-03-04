package app.web;

import app.follow.service.FollowService;
import app.message.model.Message;
import app.message.service.MessageService;
import app.notification.service.NotificationService;
import app.security.AuthenticationDetails;
import app.user.model.UserRole;
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
        User user = userService.getUserById(authenticationDetails.getUserId());
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }
        List<Message> messages = messageService.getReceivedMessagesAndMarkAsRead(user.getId());
        ModelAndView modelAndView = new ModelAndView("inbox");
        modelAndView.addObject("messages", messages);
        modelAndView.addObject("user", user);
        modelAndView.addObject("isAdmin", user.getRole() == UserRole.ADMIN);

        return modelAndView;
    }

    @GetMapping("/send_message")
    public ModelAndView showSendMessagePage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getUserById(authenticationDetails.getUserId());
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }
        ModelAndView modelAndView = new ModelAndView("send_message");
        modelAndView.addObject("messageRequest", new MessageRequest());
        modelAndView.addObject("user", user);
        modelAndView.addObject("isAdmin", user.getRole() == UserRole.ADMIN);

        return modelAndView;
    }

    @GetMapping("/inbox/{userId}")
    public ModelAndView showDirectMessage(@PathVariable UUID userId,
                                          @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User currentUser = userService.getUserById(authenticationDetails.getUserId());


        User recipient = userService.getUserById(userId);


        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setUsername(recipient.getUsername());


        ModelAndView modelAndView = new ModelAndView("send_message");
        modelAndView.addObject("messageRequest", messageRequest);
        modelAndView.addObject("recipient", recipient);
        modelAndView.addObject("user", currentUser);

        return modelAndView;
    }

    @PostMapping("/send")
    public ModelAndView sendMessage(
            MessageRequest messageRequest,
            BindingResult bindingResult,
            @AuthenticationPrincipal AuthenticationDetails authenticationDetails
    ) {
        User sender = userService.getUserById(authenticationDetails.getUserId());

        if (sender == null) {
            return new ModelAndView("redirect:/login");
        }

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("send_message");
            modelAndView.addObject("user", sender);
            return modelAndView;
        }

        Optional<User> recipientOpt = userService.getUserByUsername(messageRequest.getUsername());

        if (recipientOpt.isPresent()) {
            User recipient = recipientOpt.get();
            messageService.saveMessage(messageRequest.getUsername(), messageRequest.getContent(), sender);
            notificationService.createMessageNotification(recipient, sender.getUsername());
        }

        return new ModelAndView("redirect:/inbox");
    }

    @DeleteMapping("/inbox/delete/{messageId}")
    public ModelAndView deleteMessage(
            @PathVariable UUID messageId,
            @AuthenticationPrincipal AuthenticationDetails authenticationDetails
    ) {
        User user = userService.getUserById(authenticationDetails.getUserId());

        if (user == null) {
            return new ModelAndView("redirect:/login");
        }

        messageService.deleteMessage(messageId, user.getId());

        return new ModelAndView("redirect:/inbox");
    }
}