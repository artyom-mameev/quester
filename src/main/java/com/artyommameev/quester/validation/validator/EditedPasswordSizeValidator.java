package com.artyommameev.quester.validation.validator;

import com.artyommameev.quester.QuesterApplication;
import com.artyommameev.quester.dto.user.UserDto;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.validation.annotation.EditedPasswordSize;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static com.artyommameev.quester.QuesterApplication.MAX_SHORT_STRING_SIZE;
import static com.artyommameev.quester.QuesterApplication.MIN_STRING_SIZE;

/**
 * A validator that checks the size of the {@link User} password.
 *
 * @author Artyom Mameev
 */
public class EditedPasswordSizeValidator
        implements ConstraintValidator<EditedPasswordSize, UserDto> {

    @Override
    public void initialize(EditedPasswordSize constraintAnnotation) {
    }

    /**
     * Checks the size of the {@link User} password.
     * <p>
     * If length of the edited password specified in the 'password' field
     * does not match the minimum and maximum values specified in the
     * {@link QuesterApplication#MIN_STRING_SIZE} and
     * {@link QuesterApplication#MAX_SHORT_STRING_SIZE} constants, adds
     * constraint violation to the field 'password'.
     * <p>
     * If password specified in the 'password' field is null or
     * empty, constraint violation is not added because if the client
     * didn't fill in the fields with the new password, it means that they are
     * not going to change it and there is no need to check the password length.
     *
     * @param userDto a data transfer object that contain the password.
     * @param context the constraint validator context.
     * @return true if the edited password size is valid or the password is
     * null or empty, otherwise false.
     * @see UserDto
     */
    @Override
    public boolean isValid(UserDto userDto,
                           ConstraintValidatorContext context) {
        /*Edit password fields can be null or empty. If the fields are empty,
        password is not changed, therefore is no need to check fields' size.
        Only the main password field is checked here, because if the password
        fields does not match, they still will not pass the validation.
        */
        if (userDto.getPassword() == null) {
            return true;
        }
        if (userDto.getPassword().isEmpty()) {
            return true;
        }

        if (userDto.getPassword().length() < MIN_STRING_SIZE ||
                userDto.getPassword().length() > MAX_SHORT_STRING_SIZE) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("password").addConstraintViolation();

            return false;
        }

        return true;
    }
}
