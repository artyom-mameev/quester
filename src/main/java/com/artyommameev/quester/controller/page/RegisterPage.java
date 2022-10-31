package com.artyommameev.quester.controller.page;

import com.artyommameev.quester.controller.page.exception.Page_BadRequestException;
import com.artyommameev.quester.dto.user.RegisterUserDto;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

/**
 * A controller for handling a register page.
 *
 * @author Artyom Mameev
 */
@Controller
public class RegisterPage {

    private final UserService userService;

    /**
     * The constructor, through which dependencies are injected by Spring.
     *
     * @param userService a service that allows to query, interact and save
     *                    {@link User} objects
     * @see UserService
     */
    public RegisterPage(UserService userService) {
        this.userService = userService;
    }

    /**
     * Handles GET requests to endpoint '/register' and returns a register page.
     * <p>
     * Adds to the Spring MVC model:
     * <p>
     * 'registerUserDto' - a data transfer object with validation
     * mechanism.
     *
     * @param model the Spring MVC model.
     * @return a page with 'register' template.
     * @see RegisterUserDto
     */
    @RequestMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registerUserDto", new RegisterUserDto());

        return "register";
    }

    /**
     * Handles POST requests to endpoint '/register' and allows to register
     * new {@link User}.
     *
     * @param model           the Spring MVC model.
     * @param registerUserDto a data transfer object with validation mechanism.
     * @param bindingResult   the validation results.
     * @return if the validation is successful, redirects to the
     * {@link LoginPage}, otherwise returns to the same page to show
     * validation errors.
     * @throws Page_BadRequestException if {@link User} with that credentials
     *                                  already exists or the {@link User}
     *                                  credentials violates some constraints.
     */
    @PostMapping("/register")
    public String registerUserAccount(@Valid RegisterUserDto registerUserDto,
                                      BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.registerNewUserAccount(registerUserDto.getUsername(),
                    registerUserDto.getPassword(),
                    registerUserDto.getMatchingPassword(),
                    registerUserDto.getEmail());
        } catch (UserService.VerificationException |
                UserService.UserAlreadyExistsException e) {
            throw new Page_BadRequestException(e);
        }

        return "redirect:/login";
    }
}
