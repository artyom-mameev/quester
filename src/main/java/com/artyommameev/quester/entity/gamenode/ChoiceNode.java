package com.artyommameev.quester.entity.gamenode;

import com.artyommameev.quester.entity.Game;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

import javax.persistence.Embeddable;

/**
 * A {@link GameNode} domain entity. Encapsulates a {@link Game} choice that
 * can lead to a room (represented by {@link RoomNode}) with or without specific
 * conditions (represented by {@link ConditionNode}) that may trigger after some
 * state of flags (represented by {@link FlagNode}).<br>
 * All elements represent a tree-like structure organized according to
 * the "composite" pattern.
 *
 * @author Artyom Mameev
 * @see GameNode
 */
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChoiceNode extends GameNode {

    /**
     * Instantiates a new Choice Node.
     * <p>
     * After the object is created, it must be saved in the database
     * to be assigned a unique id.
     *
     * @param id   an id of the node (not a database id).
     * @param name a name of the node.
     * @throws EmptyStringException if any string parameter is empty.
     * @throws NullValueException   if any parameter is null.
     */
    public ChoiceNode(String id, String name) throws EmptyStringException,
            NullValueException {
        super(id);

        setName(name);
        setType(NodeType.getType(this));
    }

    /**
     * Edits the current Choice Node.
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

    /**
     * Adds a new {@link RoomNode} to the children of the current
     * Choice Node.
     *
     * @param id          an id of the new {@link RoomNode}.
     * @param name        a name of the new {@link RoomNode}.
     * @param description a description of the new {@link RoomNode}.
     * @throws ParentMismatchException if the current Choice Node already
     *                                 contains {@link RoomNode}.
     * @throws EmptyStringException    if any string parameter is empty.
     * @throws NullValueException      if any parameter is null.
     */
    @Override
    protected void addNewRoomNode(String id, String name, String description)
            throws EmptyStringException, ParentMismatchException,
            NullValueException {
        if (isRoomExistsAmongDirectChildren()) {
            throw new ParentMismatchException("There is already a room in " +
                    "the node " + id);
        }

        addChild(new RoomNode(id, name, description));
    }

    /**
     * Adds a new {@link ConditionNode} to the children of the current
     * Choice Node.
     *
     * @param id                 an id of the new {@link ConditionNode}.
     * @param conditionFlagId    an id of the {@link FlagNode}, which
     *                           should trigger the {@link ConditionNode} at a
     *                           specific {@link FlagNode} state.
     * @param conditionFlagState a {@link FlagNode} state at which the
     *                           {@link ConditionNode} should be triggered.
     * @throws EmptyStringException if any string parameter is empty.
     * @throws NullValueException   if any string parameter is null.
     */
    @Override
    protected void addNewConditionNode(String id, String conditionFlagId,
                                       Condition.FlagState conditionFlagState)
            throws EmptyStringException, NullValueException {
        addChild(new ConditionNode(id, conditionFlagId, conditionFlagState));
    }

    private boolean isRoomExistsAmongDirectChildren() {
        for (val child : getChildren()) {
            if (child instanceof RoomNode) {
                return true;
            }
        }

        return false;
    }
}
