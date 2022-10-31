package com.artyommameev.quester.validation.validator;

import com.artyommameev.quester.entity.gamenode.GameNode.Condition.FlagState;
import com.artyommameev.quester.validation.annotation.ValidConditionFlagState;
import lombok.val;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * A validator that checks if a certain field matches a {@link FlagState}
 * enumeration member.
 *
 * @author Artyom Mameev
 */
public class ConditionFlagStateValidator
        implements ConstraintValidator<ValidConditionFlagState, Enum<?>> {

    @Override
    public void initialize(ValidConditionFlagState annotation) {
    }

    /**
     * Checks if a certain field matches a {@link FlagState} enumeration member.
     *
     * @param field   the field that should be checked.
     * @param context the constraint validator context.
     * @return true if the field matches a {@link FlagState} enumeration
     * member, otherwise false.
     */
    @Override
    public boolean isValid(Enum<?> field, ConstraintValidatorContext context) {
        if (field == null) {
            return true;
        }

        val conditionTypePattern = Pattern.compile("ACTIVE|NOT_ACTIVE");

        val matcher = conditionTypePattern.matcher(field.name());

        return matcher.matches();
    }
}

