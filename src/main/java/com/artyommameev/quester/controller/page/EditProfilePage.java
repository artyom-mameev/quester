package com.artyommameev.quester.controller.page;

import com.artyommameev.quester.aspect.CurrentUserToModelAspect;
import com.artyommameev.quester.aspect.annotation.CurrentUserToModel;
import com.artyommameev.quester.controller.page.exception.Page_BadRequestException;
import com.artyommameev.quester.dto.user.EditProfileDto;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.security.user.ActualUser;
import com.artyommameev.quester.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

/**
 * A controller for handling a page for editing a profile details
 * of the current non-oAuth2 {@link User}.
 *
 * @author Artyom Mameev
 */
@Controller
@RequestMapping("/profile")
public class EditProfilePage {

    private final UserService userService;
    private final ActualUser actualUser;

    /**
     * The constructor, through which dependencies are injected by Spring.
     *
     * @param userService a service that allows to query, interact and save
     *                    {@link User} objects
     * @param actualUser  the {@link ActualUser} abstraction which represents
     *                    current normal or oAuth2 user.
     * @see UserService
     * @see ActualUser
     */
    public EditProfilePage(ActualUser actualUser, UserService userService) {
        this.userService = userService;
        this.actualUser = actualUser;
    }

    /**
     * Handles GET requests to endpoint '/profile' and returns
     * a page for editing profile details of the current non-oAuth2
     * {@link User}.
     * <p>
     * Adds to the Spring MVC model:
     * <p>
     * 'editProfileDto' - the data transfer object with email of the
     * current non-oAuth2 {@link User};<br>
     * 'user' - the current {@link User} object, adds via
     * {@link CurrentUserToModelAspect}.
     *
     * @param model the Spring MVC model.
     * @return a page with 'edit-profile' template.
     * @see EditProfileDto
     */
    @GetMapping
    @CurrentUserToModel
    public String showEditProfilePage(Model model) {
        model.addAttribute("editProfileDto", new EditProfileDto(
                actualUser.getEmail()));

        return "edit-profile";
    }

    /**
     * Handles PUT requests to endpoint '/profile' and allows
     * to edit profile details of the current non-oAuth2 {@link User}.
     *
     * @param model          the Spring MVC model.
     * @param editProfileDto the data transfer object with validation
     *                       mechanism.
     * @param bindingResult  the validation results.
     * @return if the validation is successful, redirects to {@link HomePage},
     * otherwise returns to the same page to show validation errors.
     * @throws Page_BadRequestException if a {@link User} with that
     *                                  credentials already exists in
     *                                  the database or the credentials
     *                                  violates some constraints.
     * @see EditProfileDto
     */
    @PutMapping
    @CurrentUserToModel
    @SuppressWarnings("unused")
    // model is actually used in CurrentUserToModel aspect
    public String editProfile(@Valid EditProfileDto editProfileDto,
                              BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "edit-profile";
        }

        try {
            userService.updateUserAccount(actualUser.getCurrentUser(),
                    editProfileDto.getEmail(), editProfileDto.getPassword(),
                    editProfileDto.getMatchingPassword());
        } catch (UserService.VerificationException |
                UserService.UserAlreadyExistsException e) {
            throw new Page_BadRequestException(e);
        }

        return "redirect:/";
    }
}
