package app.web;

import app.exceprion.DomainException;
import app.follow.service.FollowService;
import app.message.service.MessageService;
import app.notification.service.NotificationService;
import app.post.service.PostService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.LoginRequest;
import app.web.dto.PostResponse;
import app.web.dto.RegisterRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindingResult;

import java.util.List;

@Slf4j
@Controller
public class IndexController {

    private final UserService userService;
    private final PostService postService;
    private final MessageService messageService;
    private final FollowService followService;
    private final NotificationService notificationService;

    @Autowired
    public IndexController(UserService userService, PostService postService, MessageService messageService, FollowService followService, NotificationService notificationService) {
        this.userService = userService;
        this.postService = postService;
        this.messageService = messageService;
        this.followService = followService;
        this.notificationService = notificationService;
    }

    @GetMapping("/")
    public String getIndexPage() {
        return "index";
    }

    @GetMapping("/login")

        public ModelAndView getLoginPage(@RequestParam(value = "error", required = false) String errorParam) {

            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("login");
            modelAndView.addObject("loginRequest", new LoginRequest());

            if (errorParam != null) {
                modelAndView.addObject("errorMessage", "Incorrect username or password!");
            }

            return modelAndView;
    }



    @GetMapping("/register")
    public ModelAndView getRegisterPage() {
        ModelAndView modelAndView = new ModelAndView("register");
        modelAndView.addObject("registerRequest", new RegisterRequest());
        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView registerNewUser(@Valid RegisterRequest registerRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("register");
        }

        userService.register(registerRequest);
        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/home")
    public ModelAndView getHomePage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getUserById(authenticationDetails.getUserId());

        if (user == null) {
            return new ModelAndView("redirect:/login");
        }

        List<PostResponse> posts = postService.getAllPosts(user);
        int unreadMessageCount = messageService.getUnreadMessageCount(user.getId());
        List<User> suggestedUsers = followService.getSuggestedUsers(user.getId());
        List<User> followedUsers = followService.getFollowedUsers(user.getId());
        userService.setUserOnline(user.getId());

        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.addObject("user", user);
        modelAndView.addObject("isAdmin", user.getRole() == UserRole.ADMIN);
        modelAndView.addObject("posts", posts);
        modelAndView.addObject("unreadMessageCount", unreadMessageCount);
        modelAndView.addObject("suggestedUsers", suggestedUsers);
        modelAndView.addObject("followedUsers", followedUsers);
        int unreadNotifications = notificationService.getUnreadCount(user);
        modelAndView.addObject("unreadNotificationCount", unreadNotifications);

        return modelAndView;
    }


    @PostMapping("/api/heartbeat")
    @ResponseBody
    public ResponseEntity<Void> heartbeat(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getUserById(authenticationDetails.getUserId());
        if (user != null) {
            userService.setUserOnline(user.getId());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}