package app.web;

import app.exceprion.DomainException;
import app.follow.service.FollowService;
import app.post.model.Post;
import app.post.service.PostService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.UserEditRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller

public class UserController {
    private final UserService userService;
    private final FollowService followService;
    private final PostService postService;
@Autowired
    public UserController(UserService userService, FollowService followService, PostService postService) {
        this.userService = userService;
    this.followService = followService;
    this.postService = postService;
}

    @GetMapping("/profile")
    public ModelAndView getProfilePage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getUserById(authenticationDetails.getUserId());


        if (user == null) {
            return new ModelAndView("redirect:/login");
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profile");
        modelAndView.addObject("user", user);
        modelAndView.addObject("isAdmin", user.getRole() == UserRole.ADMIN);

        return modelAndView;
    }


    @GetMapping("/edit-profile")
    public ModelAndView getEditProfilePage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getUserById(authenticationDetails.getUserId());

        if (user == null) {
            return new ModelAndView("redirect:/login");
        }


        ModelAndView modelAndView = new ModelAndView("edit_profile");
        modelAndView.addObject("userEditRequest", new UserEditRequest());
        modelAndView.addObject("user", user);
        modelAndView.addObject("isAdmin", user.getRole() == UserRole.ADMIN);

        return modelAndView;
    }


    @PutMapping("/{id}/profile")
    public ModelAndView updateUserProfile(@PathVariable UUID id,
                                          @Valid UserEditRequest userEditRequest,                                           BindingResult bindingResult,
                                          @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {


        User user = userService.getUserById(authenticationDetails.getUserId());


        if (!user.getId().equals(id)) {
            throw new DomainException("You are not authorized to edit this profile.");
        }


        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("edit_profile");
            modelAndView.addObject("user", user);
            modelAndView.addObject("userEditRequest", userEditRequest);
            return modelAndView;
        }


        userService.editUserDetails(id, userEditRequest);

        return new ModelAndView("redirect:/profile");
    }



    @GetMapping("/profile/{userId}")
    public ModelAndView viewUserProfile(@PathVariable UUID userId,
                                        @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User profileUser = userService.getUserById(userId);


        User currentUser = userService.getUserById(authenticationDetails.getUserId());


        boolean isFollowing = followService.isFollowing(currentUser.getId(), profileUser.getId());


        List<Post> userPosts = postService.getPostsByUserId(profileUser.getId());


        Post latestPost = userPosts.isEmpty() ? null : userPosts.get(0);

        ModelAndView modelAndView = new ModelAndView("view_profile");
        modelAndView.addObject("profileUser", profileUser);
        modelAndView.addObject("user", currentUser);
        modelAndView.addObject("isAdmin", currentUser.getRole() == UserRole.ADMIN);
        modelAndView.addObject("isFollowing", isFollowing);
        modelAndView.addObject("userPosts", userPosts);
        modelAndView.addObject("latestPost", latestPost);

        return modelAndView;
    }
}
