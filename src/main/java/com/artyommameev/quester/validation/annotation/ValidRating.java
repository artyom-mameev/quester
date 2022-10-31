package com.artyommameev.quester.validation.annotation;

import com.artyommameev.quester.validation.validator.RatingValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation used with {@link RatingValidator}.
 *
 * @author Artyom Mameev
 */
@Target({TYPE, FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = RatingValidator.class)
@Documented
public @interface ValidRating {

    String message() default "Rating can be only a number from 1 to 5";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}