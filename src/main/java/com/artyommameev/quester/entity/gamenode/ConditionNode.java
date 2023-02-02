package com.artyommameev.quester.entity.gamenode;

import com.artyommameev.quester.entity.Game;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

import javax.persistence.Entity;

/**
 * A {@link GameNode} domain entity. Encapsulates a {@link Game} condition
 * that can trigger by a specific state of flag (represented by {@link FlagNode}).
 * A triggered condition may lead to a certain room (represented by
 * {@link RoomNode}) or contain another condition.<br>
 * All elements represent a tree-like structure organized according to
 * the "composite" pattern.
 *
 * @author Artyom Mameev
 * @see GameNode
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConditionNode extends GameNode {

    /**
     * Instantiates a new Condition Node.
     * <p>
     * After the object is created, it must be saved in the database
     * to be assigned a unique id.
     *
     * @param id                 an id of the node (not a database id).
     * @param conditionFlagId    an id of the {@link FlagNode}, which should
     *                           trigger this {@link ConditionNode} at a specific
     *                           {@link FlagNode} state.
     * @param conditionFlagState a {@link FlagNode} state at which this
     *                           {@link ConditionNode} should be triggered.
     * @throws EmptyStringException if any string parameter is empty.
     * @throws NullValueException   if any parameter is null.
     */
    public ConditionNode(String id, String conditionFlagId,
                         Condition.FlagState conditionFlagState)
            throws EmptyStringException, NullValueException {
        super(id);

        setCondition(new Condition(conditionFlagId, conditionFlagState, id));
        setType(NodeType.getType(this));
    }

    /**
     * Edits the current Condition Node.
     *
     * @param name               unused parameter, can be empty or null.
     * @param description        unused parameter, can be empty or null.
     * @param conditionFlagId    a new id of the {@link FlagNode}, which should
     *                           trigger this {@link ConditionNode} at a specific
     *                           {@link FlagNode} state.
     * @param conditionFlagState a new {@link FlagNode} state at which this
     *                           {@link ConditionNode} should be triggered.
     * @throws EmptyStringException if any string parameter is empty.
     * @throws NullValueException   if any parameter is null.
     */
    @Override
    protected void edit(String name, String description, String conditionFlagId,
                        Condition.FlagState conditionFlagState)
            throws EmptyStringException, NullValueException {
        setConditionFlagId(conditionFlagId);
        setConditionFlagState(conditionFlagState);
    }

    /**
     * Adds a new {@link RoomNode} to the children of the current
     * Condition Node.
     *
     * @param id          an id of the new {@link RoomNode}.
     * @param name        a name of the new {@link RoomNode}.
     * @param description a description of the new {@link RoomNode}.
     * @throws ParentMismatchException if the current Condition Node already
     *                                 contains {@link RoomNode}.
     * @throws EmptyStringException    if any string parameter is empty.
     * @throws NullValueException      if any parameter is null.
     */
    @Override
    protected void addNewRoomNode(String id, String name, String description)
            throws ParentMismatchException, EmptyStringException,
            NullValueException {
        if (isRoomExistsAmongDirectChildren()) {
            throw new ParentMismatchException("There is already a room in " +
                    "the node " + id);
        }

        addChild(new RoomNode(id, name, description));
    }

    /**
     * Adds a new {@link ConditionNode} to the children of the current
     * Condition Node.
     *
     * @param id                 an id of the new {@link ConditionNode}.
     * @param conditionFlagId    an id of the {@link FlagNode}, which should
     *                           trigger the {@link ConditionNode} at a specific
     *                           {@link FlagNode} state.
     * @param conditionFlagState a {@link FlagNode} state at which the
     *                           {@link ConditionNode} should be triggered.
     * @throws EmptyStringException if any string parameter is empty.
     * @throws NullValueException   if any parameter is null.
     */
    @Override
    protected void addNewConditionNode(String id, String conditionFlagId,
                                       Condition.FlagState conditionFlagState)
            throws EmptyStringException, NullValueException {
        addChild(new ConditionNode(id, conditionFlagId, conditionFlagState));
    }

    private void setConditionFlagId(String conditionFlagId)
            throws EmptyStringException, NullValueException {
        getCondition().setFlagId(conditionFlagId);
    }

    private void setConditionFlagState(Condition.FlagState conditionFlagState)
            throws NullValueException {
        getCondition().setFlagState(conditionFlagState);
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
