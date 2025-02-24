package app.web;

import app.comment.model.Comment;
import app.comment.repository.CommentRepository;
import app.post.Repository.PostRepository;
import app.user.repoistory.UserRepository;
import app.user.model.User;
import app.user.model.UserRole;
import jakarta.servlet.http.HttpSession;
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
    private CommentRepository commentRepository;

    public AdminController(UserRepository userRepository, PostRepository postRepository, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }


    @GetMapping
    public ModelAndView adminDashboard(HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null || loggedUser.getRole() != UserRole.ADMIN) {
            return new ModelAndView("redirect:/home");
        }

        long userCount = userRepository.count();
        long postCount = postRepository.count();
        long onlineUsers = userRepository.countOnlineUsers();
        long commentCount=commentRepository.count();
        long messageCount = 0;

        ModelAndView modelAndView = new ModelAndView("admin-dashboard");
        modelAndView.addObject("userCount", userCount);
        modelAndView.addObject("postCount", postCount);
        modelAndView.addObject("commentCount", commentCount);
        modelAndView.addObject("onlineUsers", onlineUsers);

        return modelAndView;
    }


    @GetMapping("/users")
    public ModelAndView manageUsers() {
        List<User> users = userRepository.findAll();

        ModelAndView modelAndView = new ModelAndView("admin-users");
        modelAndView.addObject("users", users);

        return modelAndView;
    }


    @DeleteMapping("/users/delete/{id}")
    public ModelAndView deleteUser(@PathVariable UUID id) {
        userRepository.deleteById(id);
        return new ModelAndView("redirect:/admin/users");
    }


    @PostMapping("/users/make-admin/{id}")
    public ModelAndView makeAdmin(@PathVariable UUID id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setRole(UserRole.ADMIN);
            userRepository.save(user);
        }
        return new ModelAndView("redirect:/admin/users");
    }
    @PostMapping("/users/make-user/{id}")
    public ModelAndView makeUser(@PathVariable UUID id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setRole(UserRole.USER);
            userRepository.save(user);
        }
        return new ModelAndView("redirect:/admin/users");
    }



    @GetMapping("/posts")
    public ModelAndView managePosts() {
        ModelAndView modelAndView = new ModelAndView("admin-posts");
        modelAndView.addObject("posts", postRepository.findAll());
        return modelAndView;
    }

    
    @DeleteMapping("/posts/delete/{id}")
    public ModelAndView deletePost(@PathVariable UUID id) {
        postRepository.deleteById(id);
        return new ModelAndView("redirect:/admin/posts");
    }


    @GetMapping("/comments")
    public ModelAndView manageComments(HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null || loggedUser.getRole() != UserRole.ADMIN) {
            return new ModelAndView("redirect:/home");
        }

        List<Comment> comments = commentRepository.findAll();


        System.out.println("Comments Retrieved: " + comments.size());

        ModelAndView modelAndView = new ModelAndView("admin-comments");
        modelAndView.addObject("comments", comments);

        return modelAndView;
    }


    @DeleteMapping("/comments/delete/{id}")
    public ModelAndView deleteComment(@PathVariable UUID id) {
        commentRepository.deleteById(id);
        return new ModelAndView("redirect:/admin/comments");
    }
}

