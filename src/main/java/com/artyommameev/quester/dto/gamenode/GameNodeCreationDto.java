package com.artyommameev.quester.dto.gamenode;

import com.artyommameev.quester.dto.ConditionDto;
import com.artyommameev.quester.entity.gamenode.GameNode;
import com.artyommameev.quester.validation.annotation.ValidGameNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import static com.artyommameev.quester.QuesterApplication.MAX_SHORT_STRING_SIZE;
import static com.artyommameev.quester.QuesterApplication.MIN_STRING_SIZE;

/**
 * A data transfer object with validation mechanism used to create a
 * {@link GameNode}.
 *
 * @author Artyom Mameev
 */
@Data
@ValidGameNode
@AllArgsConstructor
@NoArgsConstructor
public class GameNodeCreationDto implements GameNodeDto {

    @NotBlank(message = "{valid.blank-field}")
    @Size(min = MIN_STRING_SIZE, max = MAX_SHORT_STRING_SIZE,
            message = "{valid.size-of-the-field-can-not-be}" + " " +
                    "{valid.less-than}" + " " + MIN_STRING_SIZE + " " +
                    "{valid.and-more-than}" + " " + MAX_SHORT_STRING_SIZE + " " +
                    "{valid.characters}")
    private String id;

    @NotBlank(message = "{valid.blank-field}")
    private String parentId;

    private String name; // validated by @ValidGameNode

    private String description; // validated by @ValidGameNode
    @Valid
    private ConditionDto condition;

    private GameNode.NodeType type;
}
