package com.artyommameev.quester.entity.gamenode;

import com.artyommameev.quester.entity.Game;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * A {@link GameNode} domain entity. Encapsulates a {@link Game} room that can
 * contain choices (represented by {@link ChoiceNode}) that lead to other rooms,
 * and flags (represented by {@link FlagNode}) that trigger conditions
 * (represented by {@link ConditionNode}).<br>
 * All elements represent a tree-like structure organized according to
 * the "composite" pattern.
 *
 * @author Artyom Mameev
 * @see GameNode
 */
@Entity
@DiscriminatorValue("RoomNode")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomNode extends GameNode {

    /**
     * Instantiates a new Room Node.
     * <p>
     * After the object is created, it must be saved in the database
     * to be assigned a unique id.
     *
     * @param id          an id of the node (not a database id).
     * @param name        a name of the node.
     * @param description a description of the node.
     * @throws EmptyStringException if any string parameter is empty.
     * @throws NullValueException   if any parameter is null.
     */
    public RoomNode(String id, String name, String description)
            throws EmptyStringException, NullValueException {
        super(id);

        setName(name);
        setDescription(description);
        setType(NodeType.getType(this));
    }

    /**
     * Edits the current Room Node.
     *
     * @param name               a new name of the node.
     * @param description        a new description of the node.
     * @param conditionFlagId    unused parameter, can be empty or null.
     * @param conditionFlagState unused parameter, can be empty or null.
     * @throws EmptyStringException if any string parameter is empty.
     * @throws NullValueException   if any parameter is null.
     */
    @Override
    protected void edit(String name, String description, String conditionFlagId,
                        Condition.FlagState conditionFlagState)
            throws EmptyStringException, NullValueException {
        setName(name);
        setDescription(description);
    }

    /**
     * Adds a new {@link ChoiceNode} to the children of the current
     * Room Node.
     *
     * @param id   an id of the new {@link ChoiceNode}.
     * @param name a name of the new {@link ChoiceNode}.
     * @throws EmptyStringException if any string parameter is empty.
     * @throws NullValueException   if any parameter is null.
     */
    @Override
    protected void addNewChoiceNode(String id, String name)
            throws EmptyStringException, NullValueException {
        addChild(new ChoiceNode(id, name));
    }

    /**
     * Adds a new {@link FlagNode} to the children of the current
     * Room Node.
     *
     * @param id   an id of the new {@link FlagNode}.
     * @param name a name of the new {@link FlagNode}.
     * @throws EmptyStringException if any string parameter is empty.
     * @throws NullValueException   if any parameter is null.
     */
    @Override
    protected void addNewFlagNode(String id, String name)
            throws EmptyStringException, NullValueException {
        addChild(new FlagNode(id, name));
    }


}
