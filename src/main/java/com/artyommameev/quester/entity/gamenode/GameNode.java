package com.artyommameev.quester.entity.gamenode;

import com.artyommameev.quester.entity.Game;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.artyommameev.quester.QuesterApplication.*;

/**
 * A game node domain entity. Encapsulates a tree-like structure consisting of
 * {@link Game} elements that can contain other game elements, organized
 * according to the "composite" pattern.
 *
 * @author Artyom Mameev
 * @see RoomNode
 * @see ChoiceNode
 * @see FlagNode
 * @see ConditionNode
 */
@Embeddable
// more appropriate for the 'composite' pattern
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// for lazy loading when serializing to json
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public abstract class GameNode {

    @ElementCollection(fetch = FetchType.LAZY)
    @OrderColumn(nullable = false)
    private final List<GameNode> children = new ArrayList<>();
    @Column(nullable = false, updatable = false)
    @Getter
    @Size(min = MIN_STRING_SIZE, max = MAX_SHORT_STRING_SIZE)
    private String id;
    @Getter
    @Size(min = MIN_STRING_SIZE, max = MAX_SHORT_STRING_SIZE)
    private String name;
    @Getter
    @Size(min = MIN_STRING_SIZE, max = MAX_LONG_STRING_SIZE)
    private String description;
    @Column(nullable = false, updatable = false)
    @Getter
    @Setter(AccessLevel.PROTECTED)
    @Enumerated(EnumType.STRING)
    private NodeType type;
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private Condition condition;

    /**
     * Instantiates a new Game Node.
     *
     * @param id an id of the game node.
     * @throws EmptyStringException if the id is empty.
     * @throws NullValueException   if the id is null.
     */
    public GameNode(String id) throws EmptyStringException, NullValueException {
        setId(id);
    }

    /**
     * Returns all children of the node.
     *
     * @return all children of the node in an unmodifiable list.
     */
    public List<GameNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Adds a new node to children of the current node or one of
     * the current node's children.
     * <p>
     * Calls methods that are overridden in specific GameNode implementations:
     * <p>
     * {@link GameNode#addNewRoomNode(String, String, String)}
     * {@link GameNode#addNewChoiceNode(String, String)}
     * {@link GameNode#addNewFlagNode(String, String)}
     * {@link GameNode#addNewConditionNode(String, String, Condition.FlagState)}
     * <p>
     * Depending on the node type, certain method parameters may not be used
     * and therefore may be omitted. See the documentation of specific GameNode
     * implementations to see constraints of adding particular node types.
     *
     * @param id                 an id of the new node.
     * @param parentId           an id of the new node's parent.
     * @param name               a name of the new node.
     * @param description        a description of the new node.
     * @param type               a type of the new node.
     * @param conditionFlagId    an id of the {@link FlagNode}, which should
     *                           trigger the new node at a specific
     *                           {@link FlagNode} state (only for
     *                           {@link ConditionNode}).
     * @param conditionFlagState a {@link FlagNode} state at which the new
     *                           node should be triggered (only for
     *                           {@link ConditionNode}).
     * @throws AlreadyExistsException   if a node with the given id
     *                                  already exists.
     * @throws ParentNotExistsException if the parent node with the
     *                                  given parent id does not exists.
     * @throws ParentMismatchException  if a node with the given
     *                                  type cannot be added to the
     *                                  parent node with the given parent id.
     * @throws FlagNotExistsException   if a {@link FlagNode} with
     *                                  the given condition flag id
     *                                  does not exists.
     * @throws EmptyStringException     if any string parameter is empty.
     * @throws NullValueException       if a parameter needed to create a
     *                                  node of the given type is null.
     * @see RoomNode
     * @see ChoiceNode
     * @see FlagNode
     * @see Condition
     */
    public void addNodeToChildren(String id, String parentId, String name,
                                  String description, NodeType type,
                                  String conditionFlagId,
                                  Condition.FlagState conditionFlagState)
            throws FlagNotExistsException, ParentNotExistsException,
            ParentMismatchException, EmptyStringException,
            AlreadyExistsException, NullValueException {
        if (id == null) {
            throw new NullValueException("Id cannot be null");
        }
        if (parentId == null) {
            throw new NullValueException("Parent id cannot be null");
        }
        if (type == null) {
            throw new NullValueException("Type cannot be null");
        }

        val alreadyExistsNode = getNode(id);

        if (alreadyExistsNode != null) { // if a node with such id already exists
            throw new AlreadyExistsException("The node with id " + id +
                    " already exists");
        }

        val parentNode = getNode(parentId);

        if (parentNode == null) {
            throw new ParentNotExistsException("The parent " + parentId +
                    " does not exists");
        }

        switch (type) {
            case ROOM:
                parentNode.addNewRoomNode(id, name, description);
                break;
            case CHOICE:
                parentNode.addNewChoiceNode(id, name);
                break;
            case FLAG:
                parentNode.addNewFlagNode(id, name);
                break;
            case CONDITION:
                if (isFlagDoesNotExists(conditionFlagId)) {
                    throw new FlagNotExistsException("The flag " +
                            conditionFlagId + " does not exists");
                }

                parentNode.addNewConditionNode(id, conditionFlagId,
                        conditionFlagState);
                break;
        }
    }

    /**
     * A method to create and add a new {@link RoomNode} to the children of
     * the current node.
     * <p>
     * Standard realization just throws {@link ParentMismatchException}.
     * See specific GameNode implementations.
     *
     * @param id          an id of the new {@link RoomNode}.
     * @param name        a name of the new {@link RoomNode}.
     * @param description a description of the new {@link RoomNode}.
     * @throws ParentMismatchException if the new {@link RoomNode} cannot be
     *                                 added to the current GameNode
     *                                 implementation.
     * @throws EmptyStringException    if any string parameter is empty.
     * @throws NullValueException      if any parameter is null.
     * @see RoomNode
     * @see ChoiceNode
     * @see FlagNode
     * @see Condition
     */
    protected void addNewRoomNode(String id, String name,
                                  String description) throws ParentMismatchException,
            EmptyStringException, NullValueException {
        throw new ParentMismatchException("Room node cannot be added to node " +
                "with type " + type);
    }

    /**
     * A method to create and add a new {@link ChoiceNode} to the children of the
     * current node.
     * <p>
     * Standard realization just throws {@link ParentMismatchException}.
     * See specific GameNode implementations.
     *
     * @param id   an id of the new {@link ChoiceNode}.
     * @param name a name of the new {@link ChoiceNode}.
     * @throws ParentMismatchException if the new {@link ChoiceNode} cannot be
     *                                 added to the current GameNode
     *                                 implementation.
     * @throws EmptyStringException    if any string parameter is empty.
     * @throws NullValueException      if any parameter is null.
     */
    protected void addNewChoiceNode(String id, String name)
            throws ParentMismatchException, EmptyStringException,
            NullValueException {
        throw new ParentMismatchException("Choice node cannot be added to node " +
                "with type " + type);
    }

    /**
     * A method to create and add a new {@link FlagNode} to the children of the
     * current node.
     * <p>
     * Standard realization just throws {@link ParentMismatchException}.
     * See specific GameNode implementations.
     *
     * @param id   an id of the new {@link FlagNode}.
     * @param name a name of the new {@link FlagNode}.
     * @throws ParentMismatchException if the new {@link FlagNode} cannot be
     *                                 added to the current GameNode
     *                                 implementation.
     * @throws EmptyStringException    if any string parameter is empty.
     * @throws NullValueException      if any parameter is null.
     */
    protected void addNewFlagNode(String id, String name)
            throws ParentMismatchException, EmptyStringException,
            NullValueException {
        throw new ParentMismatchException("Flag node cannot be added to node " +
                "with type " + type);
    }

    /**
     * A method to create and add new {@link ConditionNode} to the children of
     * the current node.
     * <p>
     * Standard realization just throws {@link ParentMismatchException}.
     * See specific GameNode implementations.
     *
     * @param id                 an id of the new {@link ConditionNode}.
     * @param conditionFlagId    an id of the {@link FlagNode}, which should
     *                           trigger new {@link ConditionNode} at a specific
     *                           {@link FlagNode} state.
     * @param conditionFlagState a {@link FlagNode} state at which new
     *                           {@link ConditionNode} should be triggered.
     * @throws ParentMismatchException if the new {@link ConditionNode} cannot be
     *                                 added to the current GameNode
     *                                 implementation.
     * @throws EmptyStringException    if any string parameter is empty.
     * @throws NullValueException      if any parameter is null.
     */
    protected void addNewConditionNode(String id,
                                       String conditionFlagId,
                                       Condition.FlagState conditionFlagState)
            throws ParentMismatchException, EmptyStringException,
            NullValueException {
        throw new ParentMismatchException("Condition node cannot be added to " +
                "node with type " + type);
    }

    /**
     * Edits the current node or one of the current node's children.
     * <p>
     * Calls method
     * {@link GameNode#edit(String, String, String, Condition.FlagState)}
     * that is overridden in specific GameNode implementations.
     * <p>
     * Depending on the node type, certain method parameters may not be used
     * and therefore may be omitted. See the documentation of specific GameNode
     * implementations to see constraints of adding particular node types.
     *
     * @param nodeId             an id of the node that should be edited.
     * @param name               a new name of the node.
     * @param description        a new description of the node.
     * @param conditionFlagId    a new id of the {@link FlagNode}, which
     *                           should trigger the new node at a
     *                           specific {@link FlagNode} state (only for
     *                           {@link ConditionNode}).
     * @param conditionFlagState a new {@link FlagNode} state at which
     *                           the new node should be triggered (only for
     *                           {@link ConditionNode}).
     * @throws NodeNotFoundException  if the node does not exists.
     * @throws FlagNotExistsException if the {@link FlagNode} with
     *                                the given condition flag id
     *                                does not exists.
     * @throws EmptyStringException   if any string parameter is empty.
     * @throws NullValueException     if any parameter is null.
     * @see RoomNode
     * @see ChoiceNode
     * @see FlagNode
     * @see ConditionNode
     */
    public void editNode(String nodeId, String name, String description,
                         String conditionFlagId, Condition.FlagState
                                 conditionFlagState)
            throws NodeNotFoundException, EmptyStringException,
            FlagNotExistsException, NullValueException {
        if (nodeId == null) {
            throw new NullValueException("Node id cannot be null");
        }

        val nodeToEdit = getNode(nodeId);

        if (nodeToEdit == null) {
            throw new NodeNotFoundException("The node " + nodeId +
                    " is not found.");
        }
        if (nodeToEdit instanceof ConditionNode &&
                isFlagDoesNotExists(conditionFlagId)) {
            throw new FlagNotExistsException("The flag with id " +
                    conditionFlagId + " does not exist");
        }

        nodeToEdit.edit(name, description, conditionFlagId, conditionFlagState);
    }

    /**
     * An abstract method used to to edit the current node.
     * <p>
     * Depending on the node type, certain method parameters may not be used
     * and therefore may be omitted. See the documentation of specific GameNode
     * implementations to see constraints of adding particular node types.
     *
     * @param name               a new name of the node.
     * @param description        a new description of the node.
     * @param conditionFlagId    a new id of the {@link FlagNode}, which
     *                           should trigger new node at a
     *                           specific {@link FlagNode} state (only for
     *                           {@link ConditionNode}).
     * @param conditionFlagState a new {@link FlagNode} state at which
     *                           the new node should be triggered (only for
     *                           {@link ConditionNode}).
     * @throws EmptyStringException if any string parameter is empty.
     * @throws NullValueException   if any parameter is null.
     * @see RoomNode
     * @see ChoiceNode
     * @see FlagNode
     * @see ConditionNode
     */
    protected abstract void edit(String name, String description,
                                 String conditionFlagId,
                                 Condition.FlagState conditionFlagState)
            throws EmptyStringException, NullValueException;

    /**
     * Removes a game node from the current node's children.
     * <p>
     * If a {@link FlagNode} is deleted, also deletes all {@link ConditionNode}s
     * that have the condition flag id that equals to the id of
     * the {@link FlagNode}.
     *
     * @param nodeId an id of the node that should be removed.
     * @throws NullValueException        if the node id is null.
     * @throws NodeNotFoundException     if the node is not found.
     * @throws RootNodeDeletingException if trying to delete the current node.
     */
    public void deleteChildNode(String nodeId) throws NodeNotFoundException,
            RootNodeDeletingException, NullValueException {
        if (nodeId == null) {
            throw new NullValueException("Node id cannot be null");
        }

        val gameNodeToDelete = getNode(nodeId);

        if (gameNodeToDelete == null) {
            throw new NodeNotFoundException("The node " + nodeId +
                    " is not found.");
        }
        if (gameNodeToDelete == this) { // cannot delete the current node
            throw new RootNodeDeletingException("Cannot delete" +
                    " the root node");
        }

        removeChild(nodeId);

        // if removing a flag, also remove corresponding condition nodes
        if (gameNodeToDelete instanceof FlagNode) {
            removeConditionsAmongChildren(gameNodeToDelete.id);
        }
    }

    private void setId(String id) throws EmptyStringException,
            NullValueException {
        if (id == null) {
            throw new NullValueException("Id cannot be null");
        }

        if (id.isEmpty()) {
            throw new EmptyStringException("Id cannot be empty");
        }

        this.id = id;
    }

    /**
     * Updates the name of the node.
     *
     * @param name a new node name.
     * @throws EmptyStringException if the new name is empty.
     * @throws NullValueException   if the new name is null.
     */
    protected void setName(String name) throws EmptyStringException,
            NullValueException {
        if (name == null) {
            throw new NullValueException("Name cannot be null");
        }
        if (name.trim().isEmpty()) {
            throw new EmptyStringException("Name cannot be empty");
        }

        this.name = name.trim();
    }

    /**
     * Updates the description of the node.
     *
     * @param description a new node description.
     * @throws EmptyStringException if the new description is empty.
     * @throws NullValueException   if the new description is null.
     */
    protected void setDescription(String description)
            throws EmptyStringException, NullValueException {
        if (description == null) {
            throw new NullValueException("Description cannot be null");
        }
        if (description.trim().isEmpty()) {
            throw new EmptyStringException("Description cannot be empty");
        }

        this.description = description.trim();
    }

    /**
     * Adds a new node to the children of the current node.
     *
     * @param gameNode the node that should be added to the children of the
     *                 current node.
     */
    protected void addChild(GameNode gameNode) {
        children.add(gameNode);
    }

    /**
     * Returns the current node or one of the current node's children
     * by node id.
     *
     * @param id the id of the node that should be returned.
     * @return the current node or one of the current node's children
     * which id is equal to the given id.
     */
    protected GameNode getNode(String id) {
        if (this.id.equals(id)) {
            return this;
        } else {
            for (val child : children) {
                val found = child.getNode(id);
                if (found != null) return found;
            }

            return null;
        }
    }

    private boolean removeChild(String id) {
        for (int i = 0, childrenSize = children.size(); i < childrenSize; i++) {
            val child = children.get(i);

            if (child.id.equals(id)) {
                children.remove(child);

                return true;
            } else {
                boolean deleted = child.removeChild(id);

                if (deleted) {
                    return true;
                }
            }
        }

        return false;
    }

    private void removeConditionsAmongChildren(String flagId) {
        for (int i = 0, childrenSize = children.size(); i < childrenSize; i++) {
            val child = children.get(i);

            if (child instanceof ConditionNode &&
                    child.condition.flagId.equals(flagId)) {
                children.remove(child);
            } else {
                child.removeConditionsAmongChildren(flagId);
            }
        }
    }

    private boolean isFlagDoesNotExists(String flagId)
            throws NullValueException {
        if (flagId == null) {
            throw new NullValueException("flagId cannot be null");
        }

        val flagNode = getNode(flagId);

        return !(flagNode instanceof FlagNode);
    }

    /**
     * An enumeration that represents a type of the game node for json
     * serialization purposes and interaction with the client.
     */
    public enum NodeType {
        /**
         * Represents {@link RoomNode}.
         */
        ROOM,
        /**
         * Represents {@link ChoiceNode}.
         */
        CHOICE,
        /**
         * Represents {@link FlagNode}.
         */
        FLAG,
        /**
         * Represents {@link ConditionNode}.
         */
        CONDITION;

        /**
         * Returns an enumeration element that represents a type of certain
         * game node.
         *
         * @param gameNode the game node whose type representation in the form
         *                 of the enumeration member should be returned.
         * @return the enumeration member that represents a type of the
         * given game node.
         */
        public static NodeType getType(GameNode gameNode) {
            if (gameNode instanceof RoomNode) {
                return ROOM;
            }
            if (gameNode instanceof ChoiceNode) {
                return CHOICE;
            }
            if (gameNode instanceof FlagNode) {
                return FLAG;
            }
            if (gameNode instanceof ConditionNode) {
                return CONDITION;
            }

            throw new IllegalArgumentException(gameNode.getClass() +
                    " is not supported");
        }
    }

    /**
     * A Condition domain entity. Encapsulates a condition to be triggered
     * after a certain state of an existing {@link FlagNode}.
     * Used in the {@link ConditionNode}.
     *
     * @author Artyom Mameev
     */
    @Embeddable
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public final static class Condition {
        @Column(nullable = false)
        @Size(min = MIN_STRING_SIZE, max = MAX_SHORT_STRING_SIZE)
        @Getter
        private String flagId;
        @Column(nullable = false)
        @Getter
        @Enumerated(EnumType.STRING)
        private FlagState flagState;
        @Column(nullable = false, updatable = false)
        @Size(min = MIN_STRING_SIZE, max = MAX_SHORT_STRING_SIZE)
        @Getter
        private String nodeId;

        /**
         * Instantiates a new Condition.
         *
         * @param flagId    an id of the {@link FlagNode}, after a certain
         *                  state of which the {@link ConditionNode} with this
         *                  condition should be triggered.
         * @param flagState a {@link FlagNode} state after which the
         *                  {@link ConditionNode} with this condition should be
         *                  triggered.
         * @param nodeId    an id of the {@link ConditionNode}, the condition
         *                  of which represents this object.
         * @throws EmptyStringException if any string parameter is empty.
         * @throws NullValueException   if any parameter is null.
         */
        public Condition(String flagId, FlagState flagState, String nodeId)
                throws EmptyStringException, NullValueException {
            setFlagId(flagId);
            setFlagState(flagState);
            setNodeId(nodeId);
        }

        /**
         * Checks if two {@link Condition} objects are equal to each other.
         * <p>
         * Objects are equal if:
         * <p>
         * - None of the objects are null and their types are the same;
         * - Their {@link Condition#flagId}, {@link Condition#flagState} and
         * {@link Condition#nodeId} are equal to each other.
         *
         * @param o the object to be checked.
         * @return true if objects are equal, otherwise false.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            Condition condition = (Condition) o;

            return flagId.equals(condition.flagId) &&
                    flagState.equals(condition.flagState) &&
                    nodeId.equals(condition.nodeId);
        }

        /**
         * Generates a hash code for the object based on the
         * {@link Condition#flagId}, {@link Condition#flagState} and
         * {@link Condition#nodeId}.
         *
         * @return the hash code for the object.
         */
        @Override
        public int hashCode() {
            return Objects.hash(flagId, flagState, nodeId);
        }

        /**
         * Updates id of the {@link FlagNode}, after a certain state of
         * which the {@link ConditionNode} with this condition should be
         * triggered.
         *
         * @param flagId a new {@link FlagNode} id, after a certain state of
         *               which the {@link ConditionNode} with this condition
         *               should be triggered.
         * @throws EmptyStringException if the new {@link FlagNode} id is empty.
         * @throws NullValueException   if the new {@link FlagNode} id is null.
         */
        protected void setFlagId(String flagId) throws EmptyStringException,
                NullValueException {
            if (flagId == null) {
                throw new NullValueException("Flag id cannot be null");
            }
            if (flagId.isEmpty()) {
                throw new EmptyStringException("Flag id cannot be empty");
            }

            this.flagId = flagId;
        }

        /**
         * Updates the {@link FlagNode} state after which the
         * {@link ConditionNode} with this condition should be triggered.
         *
         * @param flagState the new {@link FlagNode} state after which the
         *                  {@link ConditionNode} with this condition should be
         *                  triggered.
         * @throws NullValueException if the {@link FlagNode} state is null.
         */
        protected void setFlagState(FlagState flagState)
                throws NullValueException {
            if (flagState == null) {
                throw new NullValueException("Flag state cannot be null");
            }

            this.flagState = flagState;
        }

        /**
         * Updates the id of the {@link ConditionNode}, the condition of which
         * represents this object.
         *
         * @param nodeId the new {@link ConditionNode} id, the condition of
         *               which represents this object.
         * @throws EmptyStringException if the new {@link ConditionNode} id is
         *                              empty.
         * @throws NullValueException   if the new {@link ConditionNode} id is
         *                              null.
         */
        protected void setNodeId(String nodeId) throws EmptyStringException,
                NullValueException {
            if (nodeId == null) {
                throw new NullValueException("Node id cannot be null");
            }
            if (nodeId.isEmpty()) {
                throw new EmptyStringException("Node id cannot be empty");
            }

            this.nodeId = nodeId;
        }

        /**
         * Represents a {@link FlagNode} state after which the
         * {@link ConditionNode} with this condition should be triggered.
         */
        public enum FlagState {
            /**
             * Active {@link FlagNode} state.
             */
            ACTIVE,
            /**
             * Not active {@link FlagNode} state.
             */
            NOT_ACTIVE
        }
    }

    /**
     * An exception indicating that a certain {@link GameNode}'s string
     * attribute is empty.
     */
    public static class EmptyStringException extends Exception {
        /**
         * Instantiates a new Empty String Exception.
         *
         * @param s the message that indicates the cause of exception.
         */
        public EmptyStringException(String s) {
            super(s);
        }
    }

    /**
     * An exception indicating that a certain {@link GameNode}'s attribute
     * is null.
     */
    public static class NullValueException extends Exception {
        /**
         * Instantiates a new Null Value Exception.
         *
         * @param s the message that indicates the cause of exception.
         */
        public NullValueException(String s) {
            super(s);
        }
    }

    /**
     * An exception indicating that there was an attempt to delete a root
     * {@link GameNode}.
     */
    public static class RootNodeDeletingException extends Exception {
        /**
         * Instantiates a new Root Node Deleting Exception.
         *
         * @param s the message that indicates the cause of exception.
         */
        public RootNodeDeletingException(String s) {
            super(s);
        }
    }

    /**
     * An exception indicating that a certain {@link GameNode} already exists.
     */
    public static class AlreadyExistsException extends Exception {
        /**
         * Instantiates a new Already Exists Exception.
         *
         * @param s the message that indicates the cause of exception.
         */
        public AlreadyExistsException(String s) {
            super(s);
        }
    }

    /**
     * An exception indicating that a certain {@link FlagNode} is not found.
     */
    public static class FlagNotExistsException extends Exception {
        /**
         * Instantiates a new Flag Not Exists Exception.
         *
         * @param s the message that indicates the cause of exception.
         */
        public FlagNotExistsException(String s) {
            super(s);
        }
    }

    /**
     * An exception indicating that a parent of certain {@link GameNode}
     * is not found.
     */
    public static class ParentNotExistsException extends Exception {
        /**
         * Instantiates a new Parent Not Exists Exception.
         *
         * @param s the message that indicates the cause of exception.
         */
        public ParentNotExistsException(String s) {
            super(s);
        }
    }

    /**
     * An exception indicating that a certain {@link GameNode} is not found.
     */
    public static class NodeNotFoundException extends Exception {
        /**
         * Instantiates a new Node Not Found Exception.
         *
         * @param s the message that indicates the cause of exception
         */
        public NodeNotFoundException(String s) {
            super(s);
        }
    }

    /**
     * An exception indicating that a {@link GameNode} with the given type
     * cannot be added to a certain parent {@link GameNode}.
     */
    public static class ParentMismatchException extends Exception {
        /**
         * Instantiates a new Parent Mismatch Exception.
         *
         * @param s the message that indicates the cause of exception.
         */
        public ParentMismatchException(String s) {
            super(s);
        }
    }
}
