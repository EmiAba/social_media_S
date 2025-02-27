package app.web;

import app.comment.service.CommentService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.comment.model.Comment;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    @Autowired
    public CommentController(CommentService commentService, UserService userService) {
        this.commentService = commentService;
        this.userService = userService;
    }


    @PostMapping("/add")
    public ModelAndView addComment(@RequestParam UUID postId, @RequestParam String content,@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user=userService.getById(authenticationDetails.getUserId());
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }

        commentService.createComment(postId, user, content);
        return new ModelAndView("redirect:/home");
    }


    @GetMapping("/post/{postId}")
    @ResponseBody
    public List<Comment> getComments(@PathVariable UUID postId) {
        return commentService.getCommentsForPost(postId);
    }
}
