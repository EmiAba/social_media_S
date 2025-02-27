package app.web;

import app.follow.service.FollowService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/follow")
public class FollowController {

    private final FollowService followService;
    private final UserService userService;

    @Autowired
    public FollowController(FollowService followService, UserService userService) {
        this.followService = followService;
        this.userService = userService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<String> followUser(@PathVariable UUID userId, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user=userService.getById(authenticationDetails.getUserId());

        if (user== null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        followService.followUser(user.getId(), userId);
        return ResponseEntity.ok("User followed successfully");
    }


    @PostMapping("/unfollow/{userId}")
    public ResponseEntity<String> unfollowUser(@PathVariable UUID userId,
                                               @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getById(authenticationDetails.getUserId());
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        followService.unfollowUser(user.getId(), userId);
        return ResponseEntity.ok("User unfollowed successfully");
    }
}
