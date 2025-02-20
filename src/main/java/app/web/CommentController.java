package app.web;

import app.comment.service.CommentService;
import app.user.model.User;
import app.comment.model.Comment;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }


    @PostMapping("/add")
    public ModelAndView addComment(@RequestParam UUID postId, @RequestParam String content, HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
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
