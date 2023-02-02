package com.artyommameev.quester.entity.gamenode;

import com.artyommameev.quester.entity.Game;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

/**
 * A {@link GameNode} domain entity. Encapsulates a {@link Game} flag that can
 * trigger specific conditions (represented by {@link ConditionNode}).<br>
 * All elements represent a tree-like structure organized according to
 * the "composite" pattern.
 * <p>
 * This node type cannot contain other elements.
 *
 * @author Artyom Mameev
 * @see GameNode
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FlagNode extends GameNode {

    /**
     * Instantiates a new Flag Node.
     * <p>
     * After the object is created, it must be saved in the database
     * to be assigned a unique id.
     *
     * @param id   an id of the node (not a database id).
     * @param name a name of the node.
     * @throws EmptyStringException if any string parameter is empty.
     * @throws NullValueException   if any parameter is null.
     */
    public FlagNode(String id, String name) throws EmptyStringException,
            NullValueException {
        super(id);

        setName(name);
        setType(NodeType.getType(this));
    }

    /**
     * Edits the current Flag Node.
     *
     * @param name               a new name of the node.
     * @param description        unused parameter, can be empty or null.
     * @param conditionFlagId    unused parameter, can be empty or null.
     * @param conditionFlagState unused parameter, can be empty or null.
     * @throws EmptyStringException if the name is empty.
     * @throws NullValueException   if the name is null.
     */
    @Override
    protected void edit(String name, String description,
                        String conditionFlagId, Condition.FlagState
                                conditionFlagState)
            throws EmptyStringException, NullValueException {

        setName(name);
    }
}
