package app.web;

import app.exceprion.DomainException;
import app.like.service.LikeActService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/likes")
public class LikeActController {
    private final LikeActService likeActService;
    private final UserService userService;

    public LikeActController(LikeActService likeActService, UserService userService) {
        this.likeActService = likeActService;
        this.userService = userService;
    }

    @PostMapping("/{postId}/toggle")
    public String toggleLike(
            @PathVariable UUID postId,
            @AuthenticationPrincipal AuthenticationDetails authenticationDetails,
            @RequestHeader(value = "Referer", required = false) String referer
    ) {
        User user = userService.getUserById(authenticationDetails.getUserId());

        if (user == null) {
            return "redirect:/login";
        }

        likeActService.toggleLike(postId, user);

        return "redirect:" + (referer != null ? referer : "/home");
    }

    @GetMapping("/{postId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getLikeStatus(@PathVariable UUID postId, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = null;
        if (authenticationDetails != null) {
         user = userService.getUserById(authenticationDetails.getUserId());
        }

        boolean liked = likeActService.hasUserLikedPost(postId, user);
        long likeCount = likeActService.countLikes(postId);

        Map<String, Object> response = new HashMap<>();
        response.put("liked", liked);
        response.put("likeCount", likeCount);

        return ResponseEntity.ok(response);
    }
}