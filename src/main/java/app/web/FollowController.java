package app.web;

import app.follow.service.FollowService;
import app.user.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/follow")
public class FollowController {

    private final FollowService followService;

    @Autowired
    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<String> followUser(@PathVariable UUID userId, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedUser");
        if (loggedInUser == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        followService.followUser(loggedInUser.getId(), userId);
        return ResponseEntity.ok("User followed successfully");
    }


    @PostMapping("/unfollow/{userId}")
    public ResponseEntity<String> unfollowUser(@PathVariable UUID userId, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedUser");
        if (loggedInUser == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        followService.unfollowUser(loggedInUser.getId(), userId);
        return ResponseEntity.ok("User unfollowed successfully");
    }
}
