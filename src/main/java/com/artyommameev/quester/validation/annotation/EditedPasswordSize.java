package com.artyommameev.quester.validation.annotation;

import com.artyommameev.quester.validation.validator.EditedPasswordSizeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.artyommameev.quester.QuesterApplication.MAX_SHORT_STRING_SIZE;
import static com.artyommameev.quester.QuesterApplication.MIN_STRING_SIZE;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation used with {@link EditedPasswordSizeValidator}.
 *
 * @author Artyom Mameev
 */
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = EditedPasswordSizeValidator.class)
@Documented
public @interface EditedPasswordSize {
    String message() default "The password cannot be less than " +
            MIN_STRING_SIZE + " characters and more than " +
            MAX_SHORT_STRING_SIZE + " characters!";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
