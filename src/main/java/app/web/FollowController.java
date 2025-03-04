package app.web;

import app.exceprion.DomainException;
import app.follow.service.FollowService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
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
    public String followUser(@PathVariable UUID userId,
                             @AuthenticationPrincipal AuthenticationDetails authenticationDetails,
                             HttpServletRequest request) {
        User user = userService.getUserById(authenticationDetails.getUserId());
        if (user == null) {
            return "redirect:/login";
        }


        if (!user.getId().equals(userId)) {
            followService.followUser(user.getId(), userId);
        }


        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/home");
    }

    @PostMapping("/unfollow/{userId}")
    public String unfollowUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthenticationDetails authenticationDetails,
            @RequestHeader(value = "Referer", required = false) String referer
    ) {
        User user = userService.getUserById(authenticationDetails.getUserId());

        if (user == null) {
            return "redirect:/login";
        }

        followService.unfollowUser(user.getId(), userId);

        return "redirect:" + (referer != null ? referer : "/home");
    }
}