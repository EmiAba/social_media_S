package app.web;

import app.follow.service.FollowService;
import app.like.service.LikeActService;
import app.post.model.Post;
import app.post.service.PostService;
import app.comment.service.CommentService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.PostRequest;
import app.web.dto.PostResponse;
import app.comment.model.Comment;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;
    private final FollowService followService;
    private final UserService userService;
    private final LikeActService likeActService;

    @Autowired
    public PostController(PostService postService, CommentService commentService, FollowService followService, UserService userService, LikeActService likeActService) {
        this.postService = postService;
        this.commentService = commentService;
        this.followService = followService;
        this.userService = userService;
        this.likeActService = likeActService;
    }

    @PostMapping("/create")
    public String createPost(@ModelAttribute PostRequest request, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getById(authenticationDetails.getUserId());
        if (user == null) {
            return "redirect:/login";
        }
        postService.createPost(request, user);
        return "redirect:/posts/home";
    }

    @GetMapping("/home")
    public ModelAndView getHomePage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getById(authenticationDetails.getUserId());
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }

        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.addObject("user", user);
        modelAndView.addObject("postRequest", new PostRequest());
        modelAndView.addObject("posts", postService.getAllPosts(user));
        modelAndView.addObject("suggestedUsers", followService.getSuggestedUsers(user.getId()));
        modelAndView.addObject("followedUsers", followService.getFollowedUsers(user.getId()));

        return modelAndView;
    }

    @GetMapping("/post/{id}/comments")
    public ModelAndView getPostWithComments(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getById(authenticationDetails.getUserId());
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }

        Post post = postService.getPostEntityById(id);
        List<Comment> comments = commentService.getCommentsForPost(id);
        PostResponse postResponse = postService.convertToResponse(post);

        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.addObject("user", user); // Add the user object to the model
        modelAndView.addObject("post", postResponse);
        modelAndView.addObject("comments", comments);
        modelAndView.addObject("suggestedUsers", followService.getSuggestedUsers(user.getId()));
        modelAndView.addObject("followedUsers", followService.getFollowedUsers(user.getId()));

        return modelAndView;
    }

    @GetMapping
    public String redirectToHome() {
        return "redirect:/posts/home";
    }
}