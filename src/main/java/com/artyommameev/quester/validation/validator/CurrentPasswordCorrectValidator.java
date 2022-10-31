package com.artyommameev.quester.validation.validator;

import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.security.user.ActualUser;
import com.artyommameev.quester.validation.annotation.CurrentPasswordIsCorrect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


/**
 * A validator that checks the current {@link User} password.
 *
 * @author Artyom Mameev
 */
public class CurrentPasswordCorrectValidator
        implements ConstraintValidator<CurrentPasswordIsCorrect, String> {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ActualUser actualUser;

    @Override
    public void initialize(CurrentPasswordIsCorrect constraint) {
    }

    /**
     * Checks the current {@link User} password.
     *
     * @param currentPassword the {@link User} password that should be
     *                        checked.
     * @param context         the constraint validator context.
     * @return true if the password is valid, otherwise false.
     */
    @Override
    public boolean isValid(String currentPassword,
                           ConstraintValidatorContext context) {
        return passwordEncoder.matches(currentPassword,
                actualUser.getPassword());
    }
}
