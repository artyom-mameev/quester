package com.artyommameev.quester.dto.gamenode;

import com.artyommameev.quester.dto.ConditionDto;
import com.artyommameev.quester.entity.gamenode.GameNode;
import com.artyommameev.quester.validation.annotation.ValidGameNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;

/**
 * A data transfer object with validation mechanism used to edit a
 * {@link GameNode}.
 *
 * @author Artyom Mameev
 */
@Data
@ValidGameNode
@AllArgsConstructor
@NoArgsConstructor
public class GameNodeEditingDto implements GameNodeDto {

    private String name;

    private String description;
    @Valid
    private ConditionDto condition;

    private GameNode.NodeType type;
}
