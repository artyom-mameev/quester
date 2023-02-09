package com.artyommameev.quester.controller.page;

import com.artyommameev.quester.controller.page.exception.Page_BadRequestException;
import com.artyommameev.quester.dto.ConfirmUsernameDto;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.security.user.ActualUser;
import com.artyommameev.quester.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

/**
 * A controller for handling a page for confirming username of
 * the current oAuth2 {@link User}.
 *
 * @author Artyom Mameev
 */
@Controller
public class ConfirmUsernamePage {

    private final UserService userService;
    private final ActualUser actualUser;

    /**
     * The constructor, through which dependencies are injected by Spring.
     *
     * @param userService a service that allows to query, interact and save
     *                    {@link User} objects.
     * @param actualUser  the {@link ActualUser} abstraction which represents
     *                    current normal or oAuth2 user.
     * @see UserService
     * @see ActualUser
     */
    public ConfirmUsernamePage(ActualUser actualUser, UserService userService) {
        this.userService = userService;
        this.actualUser = actualUser;
    }

    /**
     * Handles GET requests to endpoint '/confirm-username' and returns
     * a page for confirming username of the current oAuth2 {@link User}.
     * <p>
     * Adds to the Spring MVC model:
     * <p>
     * 'confirmUsernameDto' - the data transfer object with the current
     * oAuth2 {@link User} email.
     *
     * @param model the Spring MVC model
     * @return a page with 'confirm-username' template.
     * @see ConfirmUsernameDto
     */
    @RequestMapping("/confirm-username")
    public String showConfirmUsernamePage(Model model) {
        model.addAttribute("confirmUsernameDto", new ConfirmUsernameDto(
                actualUser.getGoogleUsername()));

        return "confirm-username";
    }

    /**
     * Handles POST requests to endpoint '/confirm-username' and allows
     * to confirm username of the current oAuth2 {@link User}.
     *
     * @param model              the Spring MVC model
     * @param confirmUsernameDto the data transfer object with validation
     *                           mechanism.
     * @param bindingResult      the validation results.
     * @return if the validation is successful, redirects to {@link HomePage},
     * otherwise returns to the same page to show validation errors.
     * @throws Page_BadRequestException if the username was already confirmed or
     *                                  a verification error occurs when trying
     *                                  to process the username.
     * @see ConfirmUsernameDto
     */
    @PostMapping("/confirm-username")
    @SuppressWarnings("unused") // model is using in the CurrentUserToModalAspect
    public String confirmUsername(@Valid ConfirmUsernameDto confirmUsernameDto,
                                  BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "confirm-username";
        }

        try {
            userService.confirmUsernameOfOauth2User(actualUser.getCurrentUser(),
                    confirmUsernameDto.getUsername());
        } catch (UserService.VerificationException |
                UserService.AlreadyConfirmedException e) {
            throw new Page_BadRequestException(e);
        }

        return "redirect:/";
    }
}
