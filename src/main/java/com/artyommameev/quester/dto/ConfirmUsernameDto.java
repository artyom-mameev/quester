package com.artyommameev.quester.dto;

import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.validation.annotation.UsernameNotExists;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import static com.artyommameev.quester.QuesterApplication.MAX_SHORT_STRING_SIZE;
import static com.artyommameev.quester.QuesterApplication.MIN_STRING_SIZE;

/**
 * A data transfer object with validation mechanism used to confirm the
 * username of an oAuth2 {@link User}.
 *
 * @author Artyom Mameev
 */
@Data
@AllArgsConstructor
public class ConfirmUsernameDto {

    @NotBlank(message = "{valid.blank-field}")
    @Size(min = MIN_STRING_SIZE, max = MAX_SHORT_STRING_SIZE,
            message = "{valid.size-of-the-field-can-not-be}" + " " +
                    "{valid.less-than}" + " " + MIN_STRING_SIZE + " " +
                    "{valid.and-more-than}" + " " + MAX_SHORT_STRING_SIZE + " " +
                    "{valid.characters}")
    @UsernameNotExists(message = "{valid.username-exists}")
    private String username;

}
