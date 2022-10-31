package com.artyommameev.quester.dto.user;

import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.validation.annotation.NewUserEmailNotExists;
import com.artyommameev.quester.validation.annotation.PasswordMatches;
import com.artyommameev.quester.validation.annotation.UsernameNotExists;
import com.artyommameev.quester.validation.annotation.ValidEmail;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import static com.artyommameev.quester.QuesterApplication.MAX_SHORT_STRING_SIZE;
import static com.artyommameev.quester.QuesterApplication.MIN_STRING_SIZE;

/**
 * A data transfer object with validation mechanism used to
 * register a new {@link User}.
 *
 * @author Artyom Mameev
 */
@Data
@PasswordMatches(message = "{valid.matching-password}")
@NewUserEmailNotExists(message = "{valid.email-exists}")
public class RegisterUserDto implements UserDto {

    @NotBlank(message = "{valid.username-null}")
    @Size(min = MIN_STRING_SIZE, max = MAX_SHORT_STRING_SIZE,
            message = "{valid.size-of-the-field-can-not-be}" + " " +
                    "{valid.less-than}" + " " + MIN_STRING_SIZE + " " +
                    "{valid.and-more-than}" + " " + MAX_SHORT_STRING_SIZE + " " +
                    "{valid.characters}")
    @UsernameNotExists(message = "{valid.username-exists}")
    private String username;

    @NotBlank(message = "{valid.password-null}")
    @Size(min = MIN_STRING_SIZE, max = MAX_SHORT_STRING_SIZE,
            message = "{valid.size-of-the-field-can-not-be}" + " " +
                    "{valid.less-than}" + " " + MIN_STRING_SIZE + " " +
                    "{valid.and-more-than}" + " " + MAX_SHORT_STRING_SIZE + " " +
                    "{valid.characters}")
    private String password;

    @NotBlank(message = "{valid.password-null}")
    private String matchingPassword;

    @NotBlank(message = "{valid.email-null}")
    @Size(min = MIN_STRING_SIZE, max = MAX_SHORT_STRING_SIZE,
            message = "{valid.size-of-the-field-can-not-be}" + " " +
                    "{valid.less-than}" + " " + MIN_STRING_SIZE + " " +
                    "{valid.and-more-than}" + " " + MAX_SHORT_STRING_SIZE + " " +
                    "{valid.characters}")
    @ValidEmail(message = "{valid.email}")
    private String email;
}