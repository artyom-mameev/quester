package com.artyommameev.quester.dto.user;

import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.validation.annotation.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import static com.artyommameev.quester.QuesterApplication.MAX_SHORT_STRING_SIZE;
import static com.artyommameev.quester.QuesterApplication.MIN_STRING_SIZE;

/**
 * A data transfer object with validation mechanism used to edit the
 * current {@link User} credentials.
 *
 * @author Artyom Mameev
 */
@Data
@RequiredArgsConstructor
@PasswordMatches(message = "{valid.matching-password}")
@EditedPasswordSize(message = "{valid.password-can-not-be}" + " " +
        "{valid.less-than}" + " " + MIN_STRING_SIZE + " " +
        "{valid.and-more-than}" + " " + MAX_SHORT_STRING_SIZE + " " +
        "{valid.characters}")
@NewUserEmailNotExists(message = "{valid.email-exists}")
public class EditProfileDto implements UserDto {

    @NotBlank(message = "{valid.blank-field}")
    @ValidEmail(message = "{valid.email}")
    @Size(min = MIN_STRING_SIZE, max = MAX_SHORT_STRING_SIZE,
            message = "{valid.size-of-the-field-can-not-be}" + " " +
                    "{valid.less-than}" + " " + MIN_STRING_SIZE + " " +
                    "{valid.and-more-than}" + " " + MAX_SHORT_STRING_SIZE + " " +
                    "{valid.characters}")
    private final String email;

    private String password;

    private String matchingPassword;

    @NotBlank(message = "{valid.blank-field}")
    @CurrentPasswordIsCorrect(message = "{valid.password-invalid}")
    private String currentPassword;
}