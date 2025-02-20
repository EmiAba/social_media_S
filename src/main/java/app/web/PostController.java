package app.web;

import app.follow.service.FollowService;
import app.post.model.Post;
import app.post.service.PostService;
import app.comment.service.CommentService;
import app.user.model.User;
import app.web.dto.PostRequest;
import app.web.dto.PostResponse;
import app.comment.model.Comment;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public PostController(PostService postService, CommentService commentService, FollowService followService) {
        this.postService = postService;
        this.commentService = commentService;
        this.followService = followService;
    }


    @PostMapping("/create")
    public String createPost(@ModelAttribute PostRequest request, HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null) {
            return "redirect:/login";
        }

        postService.createPost(request, user);
        return "redirect:/posts/home";
    }


    @GetMapping("/home")
    public ModelAndView getHomePage(HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return new ModelAndView("redirect:/login");
        }

        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.addObject("postRequest", new PostRequest());
        modelAndView.addObject("posts", postService.getAllPosts(loggedUser));
        modelAndView.addObject("suggestedUsers", followService.getSuggestedUsers(loggedUser.getId()));
        modelAndView.addObject("followedUsers", followService.getFollowedUsers(loggedUser.getId()));

        return modelAndView;
    }


    @GetMapping("/post/{id}/comments")
    public ModelAndView getPostWithComments(@PathVariable UUID id, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return new ModelAndView("redirect:/login");
        }


        Post post = postService.getPostEntityById(id);
        List<Comment> comments = commentService.getCommentsForPost(id);
        PostResponse postResponse = postService.convertToResponse(post);

        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.addObject("post", postResponse);
        modelAndView.addObject("comments", comments);
        modelAndView.addObject("suggestedUsers", followService.getSuggestedUsers(loggedUser.getId()));
        modelAndView.addObject("followedUsers", followService.getFollowedUsers(loggedUser.getId()));

        return modelAndView;
    }


    @GetMapping
    public String redirectToHome() {
        return "redirect:/posts/home";
    }
}
