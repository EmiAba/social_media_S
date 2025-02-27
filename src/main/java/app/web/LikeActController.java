package app.web;

import app.like.service.LikeActService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/likes")
public class LikeActController {
    private final LikeActService likeActService;
    private final UserService userService;

    public LikeActController(LikeActService likeActService, UserService userService) {
        this.likeActService = likeActService;
        this.userService = userService;
    }

    @PostMapping("/{postId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable UUID postId, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user=userService.getById(authenticationDetails.getUserId());
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        boolean liked = likeActService.toggleLike(postId, user);
        long likeCount = likeActService.countLikes(postId);

        Map<String, Object> response = new HashMap<>();
        response.put("liked", liked);
        response.put("likeCount", likeCount);

        return ResponseEntity.ok(response);
    }
}