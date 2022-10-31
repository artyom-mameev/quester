package com.artyommameev.quester.validation.annotation;

import com.artyommameev.quester.validation.validator.UsernameDoesNotExistsValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation used with {@link UsernameDoesNotExistsValidator}.
 *
 * @author Artyom Mameev
 */
@Target({TYPE, FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = UsernameDoesNotExistsValidator.class)
@Documented
public @interface UsernameNotExists {
    String message() default "Username already exists";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}