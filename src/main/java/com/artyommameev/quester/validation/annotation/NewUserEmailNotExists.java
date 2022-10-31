package com.artyommameev.quester.validation.annotation;

import com.artyommameev.quester.validation.validator.NewUserEmailDoesNotExistsValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation used with {@link NewUserEmailDoesNotExistsValidator}.
 *
 * @author Artyom Mameev
 */
@Target({TYPE, FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = NewUserEmailDoesNotExistsValidator.class)
@Documented
public @interface NewUserEmailNotExists {
    String message() default "E-mail already exists";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}