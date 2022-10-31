package com.artyommameev.quester.validation.validator;

import com.artyommameev.quester.service.UserService;
import com.artyommameev.quester.validation.annotation.UsernameNotExists;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * A validator that checks if a certain username does not exist in the
 * database.
 *
 * @author Artyom Mameev
 */
public class UsernameDoesNotExistsValidator
        implements ConstraintValidator<UsernameNotExists, String> {

    @Autowired
    private UserService userService;

    public void initialize(UsernameNotExists constraint) {
    }

    /**
     * Checks if a certain username does not exists in the database.
     *
     * @param username the username that should be checked.
     * @param context  the constraint validator context.
     * @return true if the username does not exists in the database,
     * otherwise false.
     */
    public boolean isValid(String username,
                           ConstraintValidatorContext context) {
        return !userService.usernameExists(username.trim());
    }
}
