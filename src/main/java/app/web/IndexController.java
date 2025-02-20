package app.web;

import app.follow.service.FollowService;
import app.message.service.MessageService;
import app.post.service.PostService;
import app.user.model.User;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class IndexController {

    private final UserService userService;
    private final PostService postService;
    private final MessageService messageService;
    private final FollowService followService;

    @Autowired
    public IndexController(UserService userService, PostService postService, MessageService messageService, FollowService followService) {
        this.userService = userService;
        this.postService = postService;
        this.messageService = messageService;
        this.followService = followService;
    }

    @GetMapping("/")
    public String getIndexPage() {
        return "index";
    }

    @GetMapping("/login")
    public ModelAndView getLoginPage() {
        ModelAndView modelAndView = new ModelAndView("login");
        modelAndView.addObject("loginRequest", new LoginRequest());
        return modelAndView;
    }

    @PostMapping("/login")
    public String login(@Valid LoginRequest loginRequest, BindingResult bindingResult, HttpSession session) {
        if (bindingResult.hasErrors()) {
            return "login";
        }

        User loggedInUser = userService.login(loginRequest);
        session.setAttribute("loggedUser", loggedInUser);
        userService.setUserOnline(loggedInUser.getId());
        return "redirect:/home";
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
    public ModelAndView getHomePage(HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");

        if (user == null) {
            return new ModelAndView("redirect:/login");
        }

        List<PostResponse> posts = postService.getAllPosts(user);
        int unreadMessageCount = messageService.getUnreadMessageCount(user.getId());
        List<User> suggestedUsers = followService.getSuggestedUsers(user.getId());
        List<User> followedUsers = followService.getFollowedUsers(user.getId());

        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.addObject("user", user);
        modelAndView.addObject("posts", posts);
        modelAndView.addObject("unreadMessageCount", unreadMessageCount);
        modelAndView.addObject("suggestedUsers", suggestedUsers);
        modelAndView.addObject("followedUsers", followedUsers);

        return modelAndView;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        if (user != null) {
            userService.setUserOffline(user.getId());
        }
        session.invalidate();
        return "redirect:/";
    }

    @PostMapping("/api/heartbeat")
    @ResponseBody
    public ResponseEntity<Void> heartbeat(HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        if (user != null) {
            userService.setUserOnline(user.getId());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}