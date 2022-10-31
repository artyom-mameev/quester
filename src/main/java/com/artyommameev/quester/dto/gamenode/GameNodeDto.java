package com.artyommameev.quester.dto.gamenode;

import com.artyommameev.quester.dto.ConditionDto;
import com.artyommameev.quester.entity.gamenode.GameNode;

/**
 * A root interface for {@link GameNode} data transfer objects.
 *
 * @author Artyom Mameev
 * @see GameNodeCreationDto
 * @see GameNodeEditingDto
 */
public interface GameNodeDto {

    String getName();

    String getDescription();

    ConditionDto getCondition();

    GameNode.NodeType getType();
}
