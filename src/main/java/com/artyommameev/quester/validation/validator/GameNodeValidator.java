package com.artyommameev.quester.validation.validator;

import com.artyommameev.quester.QuesterApplication;
import com.artyommameev.quester.dto.gamenode.GameNodeDto;
import com.artyommameev.quester.entity.gamenode.GameNode;
import com.artyommameev.quester.validation.annotation.ValidGameNode;
import lombok.val;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static com.artyommameev.quester.QuesterApplication.*;

/**
 * The validator that checks a various {@link GameNode} field constraints.
 *
 * @author Artyom Mameev
 */
public class GameNodeValidator
        implements ConstraintValidator<ValidGameNode, GameNodeDto> {

    private final MessageSource messageSource;
    private final String sizeError;

    /**
     * The constructor, through which dependencies are injected by Spring.
     *
     * @param messageSource the message source for internationalization purposes.
     */
    public GameNodeValidator(MessageSource messageSource) {
        this.messageSource = messageSource;

        sizeError = messageSource.getMessage(
                "valid.size-of-the-field-can-not-be",
                null, LocaleContextHolder.getLocale()) + " " +
                messageSource.getMessage("valid.less-than",
                        null, LocaleContextHolder.getLocale()) + " " +
                MIN_STRING_SIZE + " " +
                messageSource.getMessage("valid.and-more-than",
                        null, LocaleContextHolder.getLocale()) + " " +
                MAX_SHORT_STRING_SIZE + " " +
                messageSource.getMessage("valid.characters",
                        null, LocaleContextHolder.getLocale());
    }

    /**
     * Checks a various {@link GameNode} constraints.
     * <p>
     * If the 'type' field is null, adds constraint violation to the field
     * 'type'.
     * <p>
     * If the 'type' field is 'ROOM', 'CHOICE' or 'FLAG', and the 'name'
     * field is null or the field's length does not match the minimum and
     * maximum values specified in the
     * {@link QuesterApplication#MIN_STRING_SIZE} and
     * {@link QuesterApplication#MAX_SHORT_STRING_SIZE} constants, adds
     * constraint violation to the field 'name'.
     * <p>
     * If the 'type' field is 'ROOM' and 'description' field is null
     * or the field's length does not match the minimum and maximum values
     * specified in the {@link QuesterApplication#MIN_STRING_SIZE} and
     * {@link QuesterApplication#MAX_SHORT_STRING_SIZE} constants, adds
     * constraint violation to the field 'description'.
     * <p>
     * If the 'type' field is 'CONDITION', and the node's 'condition'
     * field is null, adds constraint violation to the field 'condition'.
     *
     * @param gameNodeDto a data transfer object.
     * @param context     the constraint validator context.
     * @return true if the constraints are valid, otherwise false.
     * @see GameNodeDto
     */
    @Override
    public boolean isValid(GameNodeDto gameNodeDto,
                           ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        if (isTypeNull(gameNodeDto, context)) {
            return false;
        }

        switch (gameNodeDto.getType()) {
            case ROOM:
                if (isNameNullOrEmpty(gameNodeDto, context) ||
                        isNameHasWrongSize(gameNodeDto, context) ||
                        isDescriptionNullOrEmpty(gameNodeDto, context) ||
                        isDescriptionHasWrongSize(gameNodeDto, context)) {
                    return false;
                }
                break;

            case FLAG:
            case CHOICE:
                if (isNameNullOrEmpty(gameNodeDto, context) ||
                        isNameHasWrongSize(gameNodeDto, context)) {
                    return false;
                }
                break;

            case CONDITION:
                if (isConditionNull(gameNodeDto, context)) {
                    return false;
                }
        }

        return true;
    }

    private boolean isTypeNull(GameNodeDto gameNodeDto,
                               ConstraintValidatorContext context) {
        if (gameNodeDto.getType() == null) {
            context.buildConstraintViolationWithTemplate(
                    messageSource.getMessage("valid.blank-field", null,
                            LocaleContextHolder.getLocale()))
                    .addPropertyNode("type")
                    .addConstraintViolation();

            return true;
        }

        return false;
    }

    private boolean isNameNullOrEmpty(GameNodeDto gameNodeDto,
                                      ConstraintValidatorContext context) {
        if (gameNodeDto.getName() == null ||
                gameNodeDto.getName().trim().isEmpty()) {
            context.buildConstraintViolationWithTemplate(
                    messageSource.getMessage("valid.blank-field", null,
                            LocaleContextHolder.getLocale()))
                    .addPropertyNode("name")
                    .addConstraintViolation();

            return true;
        }

        return false;
    }

    private boolean isNameHasWrongSize(GameNodeDto gameNodeDto,
                                       ConstraintValidatorContext
                                               context) {
        if (gameNodeDto.getName().length() < MIN_STRING_SIZE ||
                gameNodeDto.getName().length() > MAX_SHORT_STRING_SIZE) {
            context.buildConstraintViolationWithTemplate(sizeError)
                    .addPropertyNode("name")
                    .addConstraintViolation();

            return true;
        }

        return false;
    }

    private boolean isDescriptionNullOrEmpty(GameNodeDto gameNodeDto,
                                             ConstraintValidatorContext
                                                     context) {
        if (gameNodeDto.getDescription() == null ||
                gameNodeDto.getDescription().trim().isEmpty()) {
            context.buildConstraintViolationWithTemplate(
                    messageSource.getMessage("valid.blank-field", null,
                            LocaleContextHolder.getLocale()))
                    .addPropertyNode("description")
                    .addConstraintViolation();

            return true;
        }

        return false;
    }

    private boolean isDescriptionHasWrongSize(GameNodeDto gameNodeDto,
                                              ConstraintValidatorContext
                                                      context) {
        val descriptionSizeError = messageSource.getMessage(
                "valid.size-of-the-field-can-not-be",
                null, LocaleContextHolder.getLocale()) + " " +
                messageSource.getMessage("valid.less-than",
                        null, LocaleContextHolder.getLocale()) + " " +
                MIN_STRING_SIZE + " " +
                messageSource.getMessage("valid.and-more-than",
                        null, LocaleContextHolder.getLocale()) + " " +
                MAX_LONG_STRING_SIZE + " " +
                messageSource.getMessage("valid.characters",
                        null, LocaleContextHolder.getLocale());


        if (gameNodeDto.getDescription().length() < MIN_STRING_SIZE ||
                gameNodeDto.getDescription().length() > MAX_LONG_STRING_SIZE) {
            context.buildConstraintViolationWithTemplate(descriptionSizeError)
                    .addPropertyNode("description")
                    .addConstraintViolation();

            return true;
        }

        return false;
    }

    private boolean isConditionNull(GameNodeDto gameNodeDto,
                                    ConstraintValidatorContext context) {
        if (gameNodeDto.getCondition() == null) {
            context.buildConstraintViolationWithTemplate(
                    messageSource.getMessage("valid.blank-field", null,
                            LocaleContextHolder.getLocale()))
                    .addPropertyNode("condition")
                    .addConstraintViolation();

            return true;
        }

        return false;
    }
}
