package app.web;

import app.comment.model.Comment;
import app.comment.repository.CommentRepository;
import app.post.Repository.PostRepository;
import app.security.AuthenticationDetails;
import app.user.repoistory.UserRepository;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;

    public AdminController(UserRepository userRepository, PostRepository postRepository,
                           CommentRepository commentRepository, UserService userService) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView adminDashboard(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getById(authenticationDetails.getUserId());
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return new ModelAndView("redirect:/home");
        }

        long userCount = userRepository.count();
        long postCount = postRepository.count();
        long onlineUsers = userRepository.countOnlineUsers();
        long commentCount = commentRepository.count();
        long messageCount = 0;

        ModelAndView modelAndView = new ModelAndView("admin-dashboard");
        modelAndView.addObject("userCount", userCount);
        modelAndView.addObject("postCount", postCount);
        modelAndView.addObject("commentCount", commentCount);
        modelAndView.addObject("onlineUsers", onlineUsers);
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @GetMapping("/users")
    public ModelAndView manageUsers(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getById(authenticationDetails.getUserId());
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return new ModelAndView("redirect:/home");
        }

        List<User> users = userRepository.findAll();

        ModelAndView modelAndView = new ModelAndView("admin-users");
        modelAndView.addObject("users", users);
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @DeleteMapping("/users/delete/{id}")
    public ModelAndView deleteUser(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getById(authenticationDetails.getUserId());
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return new ModelAndView("redirect:/home");
        }

        userRepository.deleteById(id);
        return new ModelAndView("redirect:/admin/users");
    }

    @PostMapping("/users/make-admin/{id}")
    public ModelAndView makeAdmin(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User currentUser = userService.getById(authenticationDetails.getUserId());
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            return new ModelAndView("redirect:/home");
        }

        User targetUser = userRepository.findById(id).orElse(null);
        if (targetUser != null) {
            targetUser.setRole(UserRole.ADMIN);
            userRepository.save(targetUser);
        }
        return new ModelAndView("redirect:/admin/users");
    }

    @PostMapping("/users/make-user/{id}")
    public ModelAndView makeUser(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User currentUser = userService.getById(authenticationDetails.getUserId());
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            return new ModelAndView("redirect:/home");
        }

        User targetUser = userRepository.findById(id).orElse(null);
        if (targetUser != null) {
            targetUser.setRole(UserRole.USER);
            userRepository.save(targetUser);
        }
        return new ModelAndView("redirect:/admin/users");
    }

    @GetMapping("/posts")
    public ModelAndView managePosts(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getById(authenticationDetails.getUserId());
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return new ModelAndView("redirect:/home");
        }

        ModelAndView modelAndView = new ModelAndView("admin-posts");
        modelAndView.addObject("posts", postRepository.findAll());
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @DeleteMapping("/posts/delete/{id}")
    public ModelAndView deletePost(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getById(authenticationDetails.getUserId());
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return new ModelAndView("redirect:/home");
        }

        postRepository.deleteById(id);
        return new ModelAndView("redirect:/admin/posts");
    }

    @GetMapping("/comments")
    public ModelAndView manageComments(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getById(authenticationDetails.getUserId());
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return new ModelAndView("redirect:/home");
        }

        List<Comment> comments = commentRepository.findAll();
        System.out.println("Comments Retrieved: " + comments.size());

        ModelAndView modelAndView = new ModelAndView("admin-comments");
        modelAndView.addObject("comments", comments);
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @DeleteMapping("/comments/delete/{id}")
    public ModelAndView deleteComment(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getById(authenticationDetails.getUserId());
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return new ModelAndView("redirect:/home");
        }

        commentRepository.deleteById(id);
        return new ModelAndView("redirect:/admin/comments");
    }
}