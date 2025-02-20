package app.web;

import app.like.service.LikeActService;
import app.user.model.User;
import app.web.dto.LikeResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/likes")
public class LikeActController {
    private final LikeActService likeActService;

    public LikeActController(LikeActService likeActService) {
        this.likeActService = likeActService;
    }

    @PostMapping("/{postId}")
    public ResponseEntity<LikeResponse> toggleLike(@PathVariable UUID postId, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return ResponseEntity.status(401)
                    .body(new LikeResponse(false, 0L, "Unauthorized"));
        }

        boolean liked = likeActService.toggleLike(postId, loggedUser);
        long updatedCount = likeActService.countLikes(postId);

        return ResponseEntity.ok(new LikeResponse(
                liked,
                updatedCount,
                liked ? "Liked" : "Unliked"
        ));
    }

    @GetMapping("/{postId}/count")
    public ResponseEntity<Long> getLikeCount(@PathVariable UUID postId) {
        return ResponseEntity.ok(likeActService.countLikes(postId));
    }

    @GetMapping("/{postId}/status")
    public ResponseEntity<Boolean> getLikeStatus(@PathVariable UUID postId, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return ResponseEntity.status(401).body(false);
        }

        return ResponseEntity.ok(likeActService.hasUserLikedPost(postId, loggedUser));
    }

    @GetMapping("/counts")
    public ResponseEntity<Map<UUID, Long>> getBatchLikeCounts(@RequestParam List<UUID> postIds) {
        return ResponseEntity.ok(likeActService.getLikeCountsForPosts(postIds));
    }
}