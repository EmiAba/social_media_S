package app.web;

import app.client.service.ModerationService;
import app.comment.model.Comment;
import app.comment.repository.CommentRepository;
import app.message.service.MessageService;
import app.post.Repository.PostRepository;
import app.post.model.Post;
import app.post.service.PostService;
import app.security.AuthenticationDetails;
import app.user.repoistory.UserRepository;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.ContentModerationResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin")
public class AdminController {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final ModerationService moderationService;
    private final PostService postService;
    private final MessageService messageService;

    public AdminController(UserRepository userRepository, PostRepository postRepository,
                           CommentRepository commentRepository, UserService userService,
                           ModerationService moderationService, PostService postService, MessageService messageService) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.moderationService = moderationService;
        this.postService = postService;
        this.messageService = messageService;
    }

    @GetMapping
    public ModelAndView adminDashboard(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getUserById(authenticationDetails.getUserId());
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return new ModelAndView("redirect:/home");
        }

        long userCount = userRepository.count();
        long postCount = postRepository.count();
        long onlineUsers = userRepository.countOnlineUsers();
        long commentCount = commentRepository.count();
        userService.setUserOnline(user.getId());

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
        User user = userService.getUserById(authenticationDetails.getUserId());
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
        User user = userService.getUserById(authenticationDetails.getUserId());
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return new ModelAndView("redirect:/home");
        }

        userRepository.deleteById(id);
        return new ModelAndView("redirect:/admin/users");
    }

    @PostMapping("/users/make-admin/{id}")
    public ModelAndView makeAdmin(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User currentUser = userService.getUserById(authenticationDetails.getUserId());
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
        User currentUser = userService.getUserById(authenticationDetails.getUserId());
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
        User user = userService.getUserById(authenticationDetails.getUserId());
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
        User user = userService.getUserById(authenticationDetails.getUserId());
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return new ModelAndView("redirect:/home");
        }

        postRepository.deleteById(id);
        return new ModelAndView("redirect:/admin/posts");
    }

    @GetMapping("/comments")
    public ModelAndView manageComments(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getUserById(authenticationDetails.getUserId());
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return new ModelAndView("redirect:/home");
        }

        List<Comment> comments = commentRepository.findAll();

        ModelAndView modelAndView = new ModelAndView("admin-comments");
        modelAndView.addObject("comments", comments);
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @DeleteMapping("/comments/delete/{id}")
    public ModelAndView deleteComment(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getUserById(authenticationDetails.getUserId());
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return new ModelAndView("redirect:/home");
        }

        commentRepository.deleteById(id);
        return new ModelAndView("redirect:/admin/comments");
    }

    @GetMapping("/moderation")
    public ModelAndView moderationDashboard(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getUserById(authenticationDetails.getUserId());
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return new ModelAndView("redirect:/home");
        }

        List<ContentModerationResponse> pendingPosts =
                moderationService.getFilteredPendingPosts(userId, dateFrom, dateTo);

        ModelAndView modelAndView = new ModelAndView("admin-moderation");
        modelAndView.addObject("pendingPosts", pendingPosts);
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @PostMapping("/moderation/approve/{postId}")
    public ModelAndView approvePost(@PathVariable UUID postId, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getUserById(authenticationDetails.getUserId());
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return new ModelAndView("redirect:/home");
        }

        moderationService.approvePost(postId);
        postService.updatePostModerationStatus(postId);

        return new ModelAndView("redirect:/admin/moderation");
    }

    @PostMapping("/moderation/reject/{postId}")
    public ModelAndView rejectPost(@PathVariable UUID postId,
                                   @RequestParam String reason,
                                   @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User admin = userService.getUserById(authenticationDetails.getUserId());
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            return new ModelAndView("redirect:/home");
        }

        // Get the post first to find its author
        Post post = postService.getPostEntityById(postId);
        User postAuthor = post.getUser();

        // Perform moderation
        moderationService.rejectPost(postId, reason);
        postService.updatePostModerationStatus(postId);


        String messageContent = "Your post has been removed due to content policy violations.\n\n" +
                "Reason: " + reason + "\n\n" +
                "If you have any questions, please contact the admin/moderation team.";

        messageService.saveMessage(postAuthor.getUsername(), messageContent, admin);

        return new ModelAndView("redirect:/admin/moderation");
    }
    @GetMapping("/moderation/history/{userId}")
    public ModelAndView userModerationHistory(@PathVariable UUID userId,
                                              @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getUserById(authenticationDetails.getUserId());
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return new ModelAndView("redirect:/home");
        }

        User targetUser = userRepository.findById(userId).orElse(null);
        List<ContentModerationResponse> moderationHistory = moderationService.getUserModerationHistory(userId);

        ModelAndView modelAndView = new ModelAndView("admin-moderation-history");
        modelAndView.addObject("targetUser", targetUser);
        modelAndView.addObject("moderationHistory", moderationHistory);
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @GetMapping("/moderation/history")
    public ModelAndView moderationHistory(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getUserById(authenticationDetails.getUserId());
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return new ModelAndView("redirect:/home");
        }

        List<ContentModerationResponse> moderationHistory =
                moderationService.getFilteredModerationHistory(status, dateFrom, dateTo);

        ModelAndView modelAndView = new ModelAndView("admin-moderation-history");
        modelAndView.addObject("moderationHistory", moderationHistory);
        modelAndView.addObject("user", user);

        return modelAndView;
    }
}