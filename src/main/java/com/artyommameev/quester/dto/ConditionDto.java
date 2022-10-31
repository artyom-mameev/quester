package com.artyommameev.quester.dto;

import com.artyommameev.quester.entity.gamenode.GameNode;
import com.artyommameev.quester.validation.annotation.ValidConditionFlagState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static com.artyommameev.quester.QuesterApplication.MAX_SHORT_STRING_SIZE;
import static com.artyommameev.quester.QuesterApplication.MIN_STRING_SIZE;

/**
 * A data transfer object with validation mechanism used to create a
 * {@link GameNode.Condition} objects.
 *
 * @author Artyom Mameev
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConditionDto {

    @NotBlank(message = "{valid.blank-field}")
    @Size(min = MIN_STRING_SIZE, max = MAX_SHORT_STRING_SIZE,
            message = "{valid.size-of-the-field-can-not-be}" + " " +
                    "{valid.less-than}" + " " + MIN_STRING_SIZE + " " +
                    "{valid.and-more-than}" + " " + MAX_SHORT_STRING_SIZE + " " +
                    "{valid.characters}")
    private String flagId;

    @NotNull(message = "{valid.blank-field}")
    @ValidConditionFlagState(message = "{valid.condition.flag-state}")
    private GameNode.Condition.FlagState flagState;
}
