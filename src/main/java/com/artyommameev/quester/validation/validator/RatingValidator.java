package com.artyommameev.quester.validation.validator;

import com.artyommameev.quester.entity.Review;
import com.artyommameev.quester.validation.annotation.ValidRating;
import org.apache.commons.lang3.math.NumberUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * A validator that checks {@link Review.Rating} constraints.
 *
 * @author Artyom Mameev
 */
public class RatingValidator
        implements ConstraintValidator<ValidRating, String> {

    @Override
    public void initialize(ValidRating constraintAnnotation) {
    }

    /**
     * Checks {@link Review.Rating} constraints.
     * <p>
     * The rating is considered valid if it is a number between 1 and 5.
     *
     * @param ratingString the rating string that should be checked.
     * @param context      the constraint validator context.
     * @return true if the rating string is a number between 1 and 5, otherwise
     * false.
     */
    @Override
    public boolean isValid(String ratingString,
                           ConstraintValidatorContext context) {
        if (!NumberUtils.isCreatable(ratingString)) {
            return false;
        }

        int rating = Integer.parseInt(ratingString);

        return rating > 0 & rating < 6;
    }
}
