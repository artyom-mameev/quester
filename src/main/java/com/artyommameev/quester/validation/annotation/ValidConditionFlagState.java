package com.artyommameev.quester.validation.annotation;

import com.artyommameev.quester.validation.validator.ConditionFlagStateValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation used with {@link ConditionFlagStateValidator}.
 *
 * @author Artyom Mameev
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = ConditionFlagStateValidator.class)
public @interface ValidConditionFlagState {
    String message() default "Flag state must be 'ACTIVE' or 'NOT_ACTIVE'";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
