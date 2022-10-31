package com.artyommameev.quester.validation.validator;

import com.artyommameev.quester.dto.user.UserDto;
import com.artyommameev.quester.validation.annotation.PasswordMatches;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * A validator that checks if a user passwords match.
 *
 * @author Artyom Mameev
 */
public class PasswordMatchesValidator
        implements ConstraintValidator<PasswordMatches, UserDto> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
    }

    /**
     * Checks if a user passwords match.
     * <p>
     * If password specified in the 'password' field is not equals to the
     * password specified in the 'matchingPassword' field, adds constraint
     * violation to the field 'matchingPassword'.
     *
     * @param userDto a data transfer object that contain the password
     *                fields.
     * @param context the constraint validator context.
     * @return true if the passwords match, otherwise false.
     * @see UserDto
     */
    @Override
    public boolean isValid(UserDto userDto,
                           ConstraintValidatorContext context) {
        boolean isValid = userDto.getPassword().equals(
                userDto.getMatchingPassword());

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("matchingPassword")
                    .addConstraintViolation();
        }

        return isValid;
    }
}
