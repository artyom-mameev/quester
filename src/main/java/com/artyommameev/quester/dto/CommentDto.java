package com.artyommameev.quester.dto;

import com.artyommameev.quester.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import static com.artyommameev.quester.QuesterApplication.MAX_LONG_STRING_SIZE;
import static com.artyommameev.quester.QuesterApplication.MIN_STRING_SIZE;

/**
 * A data transfer object with validation mechanism used to create a
 * {@link Comment} objects.
 *
 * @author Artyom Mameev
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {

    @NotBlank(message = "{valid.blank-field}")
    @Size(min = MIN_STRING_SIZE, max = MAX_LONG_STRING_SIZE,
            message = "{valid.size-of-the-field-can-not-be}" + " " +
                    "{valid.less-than}" + " " + MIN_STRING_SIZE + " " +
                    "{valid.and-more-than}" + " " + MAX_LONG_STRING_SIZE + " " +
                    "{valid.characters}")
    private String text;
}
