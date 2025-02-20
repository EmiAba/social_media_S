package app.web;

import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.UserEditRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ModelAndView getProfilePage(HttpSession session) {

        User user = (User) session.getAttribute("loggedUser");


        if (user == null) {
            return new ModelAndView("redirect:/login");
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profile");
        modelAndView.addObject("user", user);

        return modelAndView;
    }


    @GetMapping("/edit-profile")
    public ModelAndView getEditProfilePage(HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");

        if (user == null) {
            return new ModelAndView("redirect:/login");
        }


        ModelAndView modelAndView = new ModelAndView("edit_profile");
        modelAndView.addObject("userEditRequest", new UserEditRequest());
        modelAndView.addObject("user", user);

        return modelAndView;
    }



    @PutMapping("/{id}/profile")
    public ModelAndView updateUserProfile(@PathVariable UUID id,
                                          @Valid UserEditRequest userEditRequest,
                                          BindingResult bindingResult,
                                          HttpSession session) {
        User user = userService.getById(id);

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("edit_profile");
            modelAndView.addObject("user", user);
            modelAndView.addObject("userEditRequest", userEditRequest);
            return modelAndView;
        }


        userService.editUserDetails(id, userEditRequest);


        User updatedUser = userService.getById(id);
        session.setAttribute("loggedUser", updatedUser);

        return new ModelAndView("redirect:/profile");
    }




}
