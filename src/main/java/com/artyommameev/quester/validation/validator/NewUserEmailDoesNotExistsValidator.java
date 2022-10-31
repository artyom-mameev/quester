package com.artyommameev.quester.validation.validator;

import com.artyommameev.quester.dto.user.EditProfileDto;
import com.artyommameev.quester.dto.user.UserDto;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.security.user.ActualUser;
import com.artyommameev.quester.service.UserService;
import com.artyommameev.quester.validation.annotation.NewUserEmailNotExists;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * A validator that checks if a new user e-mail does not exist in the database.
 *
 * @author Artyom Mameev
 * @see NewUserEmailDoesNotExistsValidator#isValid(UserDto, ConstraintValidatorContext)
 */
public class NewUserEmailDoesNotExistsValidator
        implements ConstraintValidator<NewUserEmailNotExists, UserDto> {

    @Autowired
    private UserService userService;
    @Autowired
    private ActualUser actualUser;

    public void initialize(NewUserEmailNotExists constraint) {
    }

    /**
     * Checks if a new user e-mail does not exist in the database.
     * <p>
     * If an email specified in the 'email' field exists in the database, adds
     * constraint violation to the field 'email'.
     * <p>
     * If the {@link UserDto} is instance of {@link EditProfileDto}, and email
     * specified in the 'email' field is equals to the current user email,
     * the constraint violation is not added, even if the email is present in
     * the database, because the {@link EditProfileDto} represents profile edit
     * fields and the 'email' field contains the current user's email, it means
     * that the client has not changed their email and it does not need to be
     * checked.
     *
     * @param userDto a data transfer object that contain the email.
     * @param context the constraint validator context.
     * @return true if the email does not exist in the database or if
     * the {@link UserDto} is instance of {@link EditProfileDto} and email
     * specified in the 'email' field is equals to the current {@link User}'s
     * email, otherwise false.
     * @see UserDto
     */
    public boolean isValid(UserDto userDto,
                           ConstraintValidatorContext context) {
        boolean isValid = !userService.emailExists(userDto.getEmail());

        /*If these are profile fields and such e-mail exists and
        equals to e-mail of the current user, that means that the user
        did not change their e-mail when editing the profile and
        there are no errors*/
        if ((userDto instanceof EditProfileDto && !isValid) &&
                userDto.getEmail().trim().equalsIgnoreCase(
                        actualUser.getEmail())) {

            isValid = true;
        }

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("email").addConstraintViolation();
        }

        return isValid;
    }
}
