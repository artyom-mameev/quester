package com.artyommameev.quester.validation.validator;

import com.artyommameev.quester.util.EmailChecker;
import com.artyommameev.quester.validation.annotation.ValidEmail;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static com.artyommameev.quester.util.EmailChecker.isEmail;

/**
 * A validator that checks e-mail format.
 *
 * @author Artyom Mameev
 */
public class EmailValidator
        implements ConstraintValidator<ValidEmail, String> {

    @Override
    public void initialize(ValidEmail constraintAnnotation) {
    }

    /**
     * Checks e-mail format. Uses {@link EmailChecker}.
     *
     * @param email   the email that should be checked.
     * @param context the constraint validator context.
     * @return true if the email format is correct, otherwise false.
     */
    @Override
    public boolean isValid(String email,
                           ConstraintValidatorContext context) {
        return email != null && isEmail(email.trim());
    }

}
