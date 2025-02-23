package app.web;

import app.like.service.LikeActService;
import app.user.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/likes")
public class LikeActController {
    private final LikeActService likeActService;

    public LikeActController(LikeActService likeActService) {
        this.likeActService = likeActService;
    }

    @PostMapping("/{postId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable UUID postId, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return ResponseEntity.status(401).build();
        }

        boolean liked = likeActService.toggleLike(postId, loggedUser);
        long likeCount = likeActService.countLikes(postId);

        Map<String, Object> response = new HashMap<>();
        response.put("liked", liked);
        response.put("likeCount", likeCount);

        return ResponseEntity.ok(response);
    }
}