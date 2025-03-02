package app.web;

import app.exceprion.DomainException;
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

import java.util.UUID;

@Controller

public class UserController {
    private final UserService userService;
@Autowired
    public UserController(UserService userService) {
        this.userService = userService;
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




}
