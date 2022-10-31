package com.artyommameev.quester.entity.gamenode;

import lombok.val;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class GameNodeTests {

    private GameNode gameNode;

    @Before
    public void setUp() throws Exception {
        gameNode = spy(GameNode.class);
        ReflectionTestUtils.setField(gameNode, "id", "testId");
    }

    @Test
    public void getChildrenReturnsUnmodifiableList() {
        val children = gameNode.getChildren();

        assertThrows(UnsupportedOperationException.class, () ->
                children.add(mock(GameNode.class)));

        assertThrows(UnsupportedOperationException.class, () ->
                children.remove(mock(GameNode.class)));

        assertThrows(UnsupportedOperationException.class, children::clear);
    }

    @Test(expected = GameNode.NullValueException.class)
    public void addNodeToChildrenThrowsNullValueExceptionIfIdIsNull() throws Exception {
        gameNode.addNodeToChildren(null, "testId",
                "testChoiceName", null, GameNode.NodeType.CHOICE,
                null, null);
    }

    @Test(expected = GameNode.NullValueException.class)
    public void addNodeToChildrenThrowsNullValueExceptionIfParentIdIsNull() throws Exception {
        gameNode.addNodeToChildren("testId2", null,
                "testChoiceName", null, GameNode.NodeType.CHOICE,
                null, null);
    }

    @Test
    public void addNodeToChildrenThrowsNullValueExceptionIfNameIsNullAndTypeIsNotCondition() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new RoomNode("testId2",
                        "testRoomName", "testRoomDesc")));

        assertThrows(GameNode.NullValueException.class, () ->
                gameNode.addNodeToChildren("testId3", "testId2",
                        null, null, GameNode.NodeType.CHOICE,
                        null, null));

        assertThrows(GameNode.NullValueException.class, () ->
                gameNode.addNodeToChildren("testId3", "testId2",
                        null, null, GameNode.NodeType.FLAG,
                        null, null));

        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new ChoiceNode("testId2",
                        "testChoiceName")));

        assertThrows(GameNode.NullValueException.class, () ->
                gameNode.addNodeToChildren("testId3", "testId2",
                        null, "testRoomDesc",
                        GameNode.NodeType.ROOM,
                        null, null));

    }

    @Test(expected = GameNode.NullValueException.class)
    public void addNodeToChildrenThrowsNullValueExceptionIfDescriptionIsNullAndTypeIsRoom() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new ChoiceNode("testId2",
                        "testChoiceName")));

        gameNode.addNodeToChildren("testId3", "testId2",
                "testRoomName", null, GameNode.NodeType.ROOM,
                null, null);
    }

    @Test(expected = GameNode.NullValueException.class)
    public void addNodeToChildrenThrowsNullValueExceptionIfTypeIsNull() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new ChoiceNode("testId2",
                        "testChoiceName")));

        gameNode.addNodeToChildren("testId3", "testId2",
                "testRoomName", "testRoomDesc",
                null, null, null);
    }

    @Test(expected = GameNode.NullValueException.class)
    public void addNodeToChildrenThrowsNullValueExceptionIfFlagIdIsNullAndTypeIsCondition() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new ChoiceNode("testId2",
                        "testChoiceName")));

        gameNode.addNodeToChildren("testId3", "testId2",
                null, null, GameNode.NodeType.CONDITION,
                null, GameNode.Condition.FlagState.ACTIVE);
    }

    @Test(expected = GameNode.NullValueException.class)
    public void addNodeToChildrenThrowsNullValueExceptionIfFlagStateIsNullAndTypeIsCondition() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Arrays.asList(new ChoiceNode("testId2",
                        "testChoiceName"), new FlagNode("testId3",
                        "testFlagName")));

        gameNode.addNodeToChildren("testId4", "testId2",
                null, null, GameNode.NodeType.CONDITION,
                "testId3", null);
    }

    @Test
    public void addNodeToChildrenAddsRoomNodeToChoiceNode() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new ChoiceNode("testId2",
                        "testChoiceName")));

        gameNode.addNodeToChildren("testId3", "testId2",
                "testRoomName", "testRoomDesc", GameNode.NodeType.ROOM,
                null, null);

        var addedNode = gameNode.getChildren().get(0)
                .getChildren().get(0);

        assertEquals("testId3", addedNode.getId());
        assertEquals("testRoomName", addedNode.getName());
        assertEquals("testRoomDesc", addedNode.getDescription());
        assertTrue(addedNode instanceof RoomNode);
    }

    @Test
    public void addNodeToChildrenAddsRoomNodeToConditionNode() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new RoomNode("testId2",
                        "testRoomName", "testRoomDesc")));

        gameNode.addNodeToChildren("testId3", "testId2",
                "testChoiceName", null, GameNode.NodeType.CHOICE,
                null, null);

        gameNode.addNodeToChildren("testId4", "testId2",
                "testFlagName", null, GameNode.NodeType.FLAG,
                null, null);

        gameNode.addNodeToChildren("testId5", "testId3",
                null, null, GameNode.NodeType.CONDITION,
                "testId4", GameNode.Condition.FlagState.ACTIVE);

        gameNode.addNodeToChildren("testId6", "testId5",
                "testRoomName2", "testRoomDesc2", GameNode.NodeType.ROOM,
                null, null);

        val addedNode = gameNode.getChildren().get(0)
                .getChildren().get(0).getChildren().get(0).getChildren().get(0);

        assertEquals("testId6", addedNode.getId());
        assertEquals("testRoomName2", addedNode.getName());
        assertEquals("testRoomDesc2", addedNode.getDescription());
        assertTrue(addedNode instanceof RoomNode);
    }

    @Test
    public void addNodeToChildrenAddsChoiceNode() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new RoomNode("testId2",
                        "testRoomName", "testRoomDesc")));

        gameNode.addNodeToChildren("testId3", "testId2",
                "testChoiceName", null, GameNode.NodeType.CHOICE,
                null, null);

        val addedNode = gameNode.getChildren().get(0)
                .getChildren().get(0);

        assertEquals("testId3", addedNode.getId());
        assertEquals("testChoiceName", addedNode.getName());
        assertTrue(addedNode instanceof ChoiceNode);
    }

    @Test
    public void addNodeToChildrenAddsFlagNode() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new RoomNode("testId2",
                        "testRoomName", "testRoomDesc")));

        gameNode.addNodeToChildren("testId3", "testId2",
                "testFlagName", null, GameNode.NodeType.FLAG,
                null, null);

        val addedNode = gameNode.getChildren().get(0)
                .getChildren().get(0);

        assertEquals("testId3", addedNode.getId());
        assertEquals("testFlagName", addedNode.getName());
        assertTrue(addedNode instanceof FlagNode);
    }

    @Test
    public void addNodeToChildrenAddsConditionNodeToChoiceNode() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new RoomNode("testId2",
                        "testRoomName", "testRoomDesc")));

        gameNode.addNodeToChildren("testId3", "testId2",
                "testFlagName", null, GameNode.NodeType.FLAG,
                null, null);

        gameNode.addNodeToChildren("testId4", "testId2",
                "testChoiceName", null, GameNode.NodeType.CHOICE,
                null, null);

        gameNode.addNodeToChildren("testId5", "testId4",
                null, null, GameNode.NodeType.CONDITION,
                "testId3", GameNode.Condition.FlagState.ACTIVE);

        val addedNode = gameNode.getChildren().get(0)
                .getChildren().get(1).getChildren().get(0);

        assertEquals("testId5", addedNode.getId());
        assertTrue(addedNode instanceof ConditionNode);
        assertEquals("testId3", addedNode.getCondition().getFlagId());
        assertEquals(GameNode.Condition.FlagState.ACTIVE,
                addedNode.getCondition().getFlagState());
        assertEquals("testId5", addedNode.getCondition().getNodeId());
    }

    @Test
    public void addNodeToChildrenAddsConditionNodeToConditionNode() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new RoomNode("testId2",
                        "testRoomName", "testRoomDesc")));

        gameNode.addNodeToChildren("testId3", "testId2",
                "testFlagName", null, GameNode.NodeType.FLAG,
                null, null);

        gameNode.addNodeToChildren("testId4", "testId2",
                "testChoiceName", null, GameNode.NodeType.CHOICE,
                null, null);

        gameNode.addNodeToChildren("testId5", "testId4",
                null, null, GameNode.NodeType.CONDITION,
                "testId3", GameNode.Condition.FlagState.ACTIVE);

        gameNode.addNodeToChildren("testId6", "testId5",
                null, null, GameNode.NodeType.CONDITION,
                "testId3", GameNode.Condition.FlagState.NOT_ACTIVE);

        val addedNode = gameNode.getChildren().get(0)
                .getChildren().get(1).getChildren().get(0).getChildren().get(0);

        assertEquals("testId6", addedNode.getId());
        assertTrue(addedNode instanceof ConditionNode);
        assertEquals("testId3", addedNode.getCondition().getFlagId());
        assertEquals(GameNode.Condition.FlagState.NOT_ACTIVE,
                addedNode.getCondition().getFlagState());
        assertEquals("testId6", addedNode.getCondition().getNodeId());
    }

    @Test(expected = GameNode.AlreadyExistsException.class)
    public void addNodeToChildrenThrowsAlreadyExistsExceptionIfIdAlreadyExists() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new ChoiceNode("testId2",
                        "testChoiceName")));

        gameNode.addNodeToChildren("testId", "testId2",
                "testRoomName", "testRoomDesc",
                GameNode.NodeType.ROOM, null,
                null);
    }

    @Test(expected = GameNode.FlagNotExistsException.class)
    public void addNodeToChildrenThrowsFlagNotExistsExceptionIfTypeIsConditionAndFlagNotExists() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new ChoiceNode("testId2",
                        "testChoiceName")));

        gameNode.addNodeToChildren("testId3", "testId2",
                null, null, GameNode.NodeType.CONDITION,
                "notExists", GameNode.Condition.FlagState.ACTIVE);
    }

    @Test(expected = GameNode.ParentNotExistsException.class)
    public void addNodeToChildrenThrowsParentNotExistsExceptionIfParentNodeNotExists() throws Exception {
        gameNode.addNodeToChildren("testId2", "notExists",
                "testFlagName", null,
                GameNode.NodeType.FLAG, null,
                null);
    }

    @Test(expected = GameNode.ParentMismatchException.class)
    public void addNodeToChildrenThrowsParentMismatchExceptionIfTypeIsRoomAndParentIsRoom() throws Exception {
        gameNode.addNodeToChildren("testId3", "testId",
                "testRoomName", "testRoomDesc",
                GameNode.NodeType.ROOM, null, null);
    }

    @Test(expected = GameNode.ParentMismatchException.class)
    public void addNodeToChildrenThrowsParentMismatchExceptionIfTypeIsRoomAndParentIsFlag() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new FlagNode("testId2",
                        "testFlagName")));

        gameNode.addNodeToChildren("testId3", "testId2",
                "testRoomName", "testRoomDesc",
                GameNode.NodeType.ROOM, null, null);
    }

    @Test(expected = GameNode.ParentMismatchException.class)
    public void addNodeToChildrenThrowsParentMismatchExceptionIfTypeIsRoomAndChoiceParentAlreadyHasRooms() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new ChoiceNode("testId2",
                        "testChoiceName")));

        gameNode.addNodeToChildren("testId3", "testId2",
                "testRoomName", "testRoomDesc", GameNode.NodeType.ROOM,
                null, null);

        gameNode.addNodeToChildren("testId4", "testId2",
                "testRoomName2", "testRoomDesc2",
                GameNode.NodeType.ROOM, null, null);
    }

    @Test(expected = GameNode.ParentMismatchException.class)
    public void addNodeToChildrenThrowsParentMismatchExceptionIfTypeIsRoomAndConditionParentAlreadyHasRooms() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new RoomNode("testId2",
                        "testRoomName", "testRoomDesc")));

        gameNode.addNodeToChildren("testId3", "testId2",
                "testChoiceName", null, GameNode.NodeType.CHOICE,
                null, null);

        gameNode.addNodeToChildren("testId4", "testId2",
                "testFlagName", null, GameNode.NodeType.FLAG,
                null, null);

        gameNode.addNodeToChildren("testId5", "testId3",
                null, null, GameNode.NodeType.CONDITION,
                "testId4", GameNode.Condition.FlagState.ACTIVE);

        gameNode.addNodeToChildren("testId6", "testId5",
                "testRoomName2", "testRoomDesc2",
                GameNode.NodeType.ROOM, null, null);

        gameNode.addNodeToChildren("testId7", "testId5",
                "testRoomName3", "testRoomDesc3",
                GameNode.NodeType.ROOM, null,
                null);
    }

    @Test(expected = GameNode.ParentMismatchException.class)
    public void addNodeToChildrenThrowsParentMismatchExceptionIfTypeIsChoiceAndParentIsFlag() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new FlagNode("testId2",
                        "testFlagName")));

        gameNode.addNodeToChildren("testId3", "testId2",
                "testChoiceName", null,
                GameNode.NodeType.CHOICE, null,
                null);
    }

    @Test(expected = GameNode.ParentMismatchException.class)
    public void addNodeToChildrenThrowsParentMismatchExceptionIfTypeIsChoiceAndParentIsChoice() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new ChoiceNode("testId2",
                        "testChoiceName")));

        gameNode.addNodeToChildren("testId3", "testId2",
                "testChoiceName2", null,
                GameNode.NodeType.CHOICE, null,
                null);
    }

    @Test(expected = GameNode.ParentMismatchException.class)
    public void addNodeToChildrenThrowsParentMismatchExceptionIfTypeIsChoiceAndParentIsCondition() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new RoomNode("testId2",
                        "testRoomName", "testRoomDesc")));

        gameNode.addNodeToChildren("testId3", "testId2",
                "testFlagName", null, GameNode.NodeType.FLAG,
                null, null);

        gameNode.addNodeToChildren("testId4", "testId2",
                "testChoiceName", null, GameNode.NodeType.CHOICE,
                null, null);

        gameNode.addNodeToChildren("testId5", "testId4",
                null, null, GameNode.NodeType.CONDITION,
                "testId3", GameNode.Condition.FlagState.ACTIVE);

        gameNode.addNodeToChildren("testId6", "testId5",
                "testChoiceName2", null,
                GameNode.NodeType.CHOICE, null,
                null);
    }

    @Test(expected = GameNode.ParentMismatchException.class)
    public void addNodeToChildrenThrowsParentMismatchExceptionIfTypeIsFlagAndParentIsFlag() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new FlagNode("testId2",
                        "testFlagName")));

        gameNode.addNodeToChildren("testId3", "testId2",
                "testFlagName2", null, GameNode.NodeType.FLAG,
                null, null);
    }

    @Test(expected = GameNode.ParentMismatchException.class)
    public void addNodeToChildrenThrowsParentMismatchExceptionIfTypeIsFlagAndParentIsChoice() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new ChoiceNode("testId2",
                        "testChoiceName")));

        gameNode.addNodeToChildren("testId3", "testId2",
                "testFlagName", null, GameNode.NodeType.FLAG,
                null, null);
    }

    @Test(expected = GameNode.ParentMismatchException.class)
    public void addNodeToChildrenThrowsParentMismatchExceptionIfTypeIsFlagAndParentIsCondition() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new RoomNode("testId2",
                        "testRoomName", "testRoomDesc")));

        gameNode.addNodeToChildren("testId3", "testId2",
                "testFlagName", null, GameNode.NodeType.FLAG,
                null, null);

        gameNode.addNodeToChildren("testId4", "testId2",
                "testChoiceName", null, GameNode.NodeType.CHOICE,
                null, null);

        gameNode.addNodeToChildren("testId5", "testId4",
                null, null, GameNode.NodeType.CONDITION,
                "testId3", GameNode.Condition.FlagState.ACTIVE);

        gameNode.addNodeToChildren("testId6", "testId5",
                "testFlagName2", null, GameNode.NodeType.FLAG,
                null, null);
    }

    @Test(expected = GameNode.ParentMismatchException.class)
    public void addNodeToChildrenThrowsParentMismatchExceptionIfTypeIsConditionAndParentIsRoom() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new RoomNode("testId2",
                        "testRoomName", "testRoomDesc")));

        gameNode.addNodeToChildren("testId3", "testId2",
                "testFlagName", null, GameNode.NodeType.FLAG,
                null, null);

        gameNode.addNodeToChildren("testId4", "testId2",
                null, null, GameNode.NodeType.CONDITION,
                "testId3", GameNode.Condition.FlagState.ACTIVE);
    }

    @Test(expected = GameNode.ParentMismatchException.class)
    public void addNodeToChildrenThrowsParentMismatchExceptionIfTypeIsConditionAndParentIsFlag() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new RoomNode("testId2",
                        "testRoomName", "testRoomDesc")));

        gameNode.addNodeToChildren("testId3", "testId2",
                "testFlagName", null, GameNode.NodeType.FLAG,
                null, null);

        gameNode.addNodeToChildren("testId4", "testId3",
                null, null, GameNode.NodeType.CONDITION,
                "testId3", GameNode.Condition.FlagState.ACTIVE);
    }

    @Test(expected = GameNode.NullValueException.class)
    public void editNodeThrowsNullValueExceptionIfNodeIdIsNull() throws Exception {
        gameNode.editNode(null, "testName", "testDesc",
                null, null);
    }

    @Test(expected = GameNode.NullValueException.class)
    public void editNodeThrowsNullValueExceptionIfTypeIsRoomAndNameIsNull() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new RoomNode("testId2",
                        "testRoomName", "testRoomDesc")));

        gameNode.editNode("testId2", null, "testDesc2",
                null, null);
    }

    @Test(expected = GameNode.NullValueException.class)
    public void editNodeThrowsNullValueExceptionIfTypeIsChoiceAndNameIsNull() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new ChoiceNode("testId2",
                        "testChoiceName")));

        gameNode.editNode("testId2", null, null,
                null, null);
    }

    @Test(expected = GameNode.NullValueException.class)
    public void editNodeThrowsNullValueExceptionIfTypeIsFlagAndNameIsNull() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new FlagNode("testId2",
                        "testFlagName")));

        gameNode.editNode("testId2", null, null,
                null, null);
    }

    @Test(expected = GameNode.NullValueException.class)
    public void editNodeThrowsNullValueExceptionIfTypeIsRoomAndDescriptionIsNull() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new RoomNode("testId2",
                        "testRoomName", "testRoomDesc")));

        gameNode.editNode("testId2", "testRoomName",
                null, null,
                null);
    }

    @Test(expected = GameNode.NullValueException.class)
    public void editNodeThrowsNullValueExceptionIfTypeIsConditionAndFlagIdIsNull() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new RoomNode("testId2",
                        "testRoomName", "testRoomDesc")));

        gameNode.addNodeToChildren("testId3", "testId2",
                "testFlagName", null, GameNode.NodeType.FLAG,
                null, null);

        gameNode.addNodeToChildren("testId4", "testId2",
                "testChoiceName", null, GameNode.NodeType.CHOICE,
                null, null);

        gameNode.addNodeToChildren("testId5", "testId4", null,
                null, GameNode.NodeType.CONDITION,
                "testId3", GameNode.Condition.FlagState.ACTIVE);

        gameNode.editNode("testId5", null, null,
                null, GameNode.Condition.FlagState.ACTIVE);
    }

    @Test(expected = GameNode.NullValueException.class)
    public void editNodeThrowsNullValueExceptionIfTypeIsConditionAndFlagStateIsNull() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new RoomNode("testId2",
                        "testRoomName", "testRoomDesc")));

        gameNode.addNodeToChildren("testId3", "testId2",
                "testFlagName", null, GameNode.NodeType.FLAG,
                null, null);

        gameNode.addNodeToChildren("testId4", "testId2",
                "testChoiceName", null, GameNode.NodeType.CHOICE,
                null, null);

        gameNode.addNodeToChildren("testId5", "testId4", null,
                null, GameNode.NodeType.CONDITION,
                "testId3", GameNode.Condition.FlagState.ACTIVE);

        gameNode.addNodeToChildren("testId6", "testId2",
                "testFlagName2", null, GameNode.NodeType.FLAG,
                null, null);

        gameNode.editNode("testId5", null, null,
                "testId6", null);
    }

    @Test(expected = GameNode.NodeNotFoundException.class)
    public void editNodeThrowsNodeNotFoundExceptionIfNodeIsNotFound() throws Exception {
        gameNode.editNode("testId2", "testName", "testDesc",
                null, null);
    }

    @Test
    public void editNodeEditsRoomNode() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new RoomNode("testId2",
                        "testRoomName", "testRoomDesc")));

        gameNode.editNode("testId2", "testName2",
                "testDesc2", null, null);

        val editedNode = gameNode.getChildren().get(0);

        assertEquals("testName2", editedNode.getName());
        assertEquals("testDesc2", editedNode.getDescription());
    }

    @Test
    public void editNodeEditsChoiceNode() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new ChoiceNode("testId2",
                        "testChoiceName")));

        gameNode.editNode("testId2", "testChoiceName2",
                null, null, null);

        val editedNode = gameNode.getChildren().get(0);

        assertEquals("testChoiceName2", editedNode.getName());
    }

    @Test
    public void editNodeEditsFlagNode() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new FlagNode("testId2",
                        "testFlagName")));


        gameNode.editNode("testId2", "testFlagName2",
                null, null, null);

        val editedNode = gameNode.getChildren().get(0);

        assertEquals("testFlagName2", editedNode.getName());
    }

    @Test
    public void editNodeEditsConditionNode() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new RoomNode("testId2",
                        "testRoomName", "testRoomDesc")));

        gameNode.addNodeToChildren("testId3", "testId2",
                "testFlagName", null, GameNode.NodeType.FLAG,
                null, null);

        gameNode.addNodeToChildren("testId4", "testId2",
                "testFlagName2", null, GameNode.NodeType.FLAG,
                null, null);

        gameNode.addNodeToChildren("testId5", "testId2",
                "testChoiceName", null, GameNode.NodeType.CHOICE,
                null, null);

        gameNode.addNodeToChildren("testId6", "testId5",
                null, null, GameNode.NodeType.CONDITION,
                "testId3", GameNode.Condition.FlagState.ACTIVE);

        gameNode.editNode("testId6", null, null,
                "testId4", GameNode.Condition.FlagState.NOT_ACTIVE);

        var editedNode = gameNode.getChildren().get(0)
                .getChildren().get(2).getChildren().get(0);

        assertEquals("testId4", editedNode.getCondition().getFlagId());
        assertEquals(GameNode.Condition.FlagState.NOT_ACTIVE,
                editedNode.getCondition().getFlagState());
    }

    @Test(expected = GameNode.FlagNotExistsException.class)
    public void editNodeThrowsFlagNotExistsExceptionIfTypeIsConditionAndFlagDoesNotExists() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new RoomNode("testId2",
                        "testRoomName", "testRoomDesc")));

        gameNode.addNodeToChildren("testId3", "testId2",
                "testFlagName", null, GameNode.NodeType.FLAG,
                null, null);

        gameNode.addNodeToChildren("testId4", "testId2",
                "testChoiceName", null, GameNode.NodeType.CHOICE,
                null, null);

        gameNode.addNodeToChildren("testId5", "testId4",
                null, null, GameNode.NodeType.CONDITION,
                "testId3", GameNode.Condition.FlagState.ACTIVE);

        gameNode.editNode("testId5", null, null,
                "testId6", GameNode.Condition.FlagState.NOT_ACTIVE);
    }

    @Test(expected = GameNode.NullValueException.class)
    public void deleteChildNodeThrowsNullValueExceptionIfIdIsNull() throws Exception {
        gameNode.deleteChildNode(null);
    }

    @Test(expected = GameNode.NodeNotFoundException.class)
    public void deleteChildNodeThrowsNodeNotFoundExceptionIfNodeIsNotFound() throws Exception {
        gameNode.deleteChildNode("testId2");
    }

    @Test(expected = GameNode.RootNodeDeletingException.class)
    public void deleteChildNodeThrowsRootNodeDeletingExceptionIfTryingToDeleteRootNode() throws Exception {
        gameNode.deleteChildNode("testId");
    }

    @Test
    public void deleteChildNodeRemovesChildNode() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new RoomNode("testId2",
                        "testRoomName", "testRoomDesc")));

        gameNode.addNodeToChildren("testId3", "testId2",
                "testChoiceName", null, GameNode.NodeType.CHOICE,
                null, null);

        gameNode.deleteChildNode("testId3");

        assertEquals(0, gameNode.getChildren().get(0)
                .getChildren().size());
    }

    @Test
    public void deleteChildNodeDeletesCorrespondingConditionsForFlagNode() throws Exception {
        ReflectionTestUtils.setField(gameNode, "children",
                Collections.singletonList(new RoomNode("testId2",
                        "testRoomName", "testRoomDesc")));

        gameNode.addNodeToChildren("testId3", "testId2",
                "testFlagName", null, GameNode.NodeType.FLAG,
                null, null);

        gameNode.addNodeToChildren("testId4", "testId2",
                "testChoiceName", null, GameNode.NodeType.CHOICE,
                null, null);

        gameNode.addNodeToChildren("testId5", "testId4", null,
                null, GameNode.NodeType.CONDITION,
                "testId3", GameNode.Condition.FlagState.ACTIVE);

        gameNode.addNodeToChildren("testId6", "testId2",
                "testChoiceName2", null, GameNode.NodeType.CHOICE,
                null, null);

        gameNode.addNodeToChildren("testId7", "testId6", null,
                null, GameNode.NodeType.CONDITION,
                "testId3", GameNode.Condition.FlagState.NOT_ACTIVE);

        gameNode.deleteChildNode("testId3");

        assertEquals(0, gameNode.getChildren().get(0)
                .getChildren().get(0).getChildren().size());
        assertEquals(0, gameNode.getChildren().get(0)
                .getChildren().get(1).getChildren().size());
    }

    @Test(expected = GameNode.NullValueException.class)
    public void GameNode_Condition_ConstructorThrowsNullValueExceptionIfFlagIdIsNull() throws Exception {
        new GameNode.Condition(null, GameNode.Condition.FlagState.ACTIVE,
                "nodeId");
    }

    @Test(expected = GameNode.EmptyStringException.class)
    public void GameNode_Condition_ConstructorThrowsEmptyStringExceptionIfFlagIdIsEmpty() throws Exception {
        new GameNode.Condition("", GameNode.Condition.FlagState.ACTIVE,
                "nodeId");
    }

    @Test(expected = GameNode.NullValueException.class)
    public void GameNode_Condition_ConstructorThrowsNullValueExceptionIfFlagStateIsNull() throws Exception {
        new GameNode.Condition("flagId", null, "nodeId");
    }

    @Test(expected = GameNode.NullValueException.class)
    public void GameNode_Condition_ConstructorThrowsNullValueExceptionIfNodeIdIsNull() throws Exception {
        new GameNode.Condition("flagId",
                GameNode.Condition.FlagState.ACTIVE, null);
    }

    @Test(expected = GameNode.EmptyStringException.class)
    public void GameNode_Condition_ConstructorThrowsEmptyStringExceptionIfNodeIdIsEmpty() throws Exception {
        new GameNode.Condition("flagId",
                GameNode.Condition.FlagState.ACTIVE, "");
    }

    @Test
    public void GameNode_Condition_ConstructorSetsAllFields() throws Exception {
        val condition = new GameNode.Condition("flagId",
                GameNode.Condition.FlagState.ACTIVE, "nodeId");

        assertEquals("flagId", condition.getFlagId());
        assertEquals(GameNode.Condition.FlagState.ACTIVE,
                condition.getFlagState());
        assertEquals("nodeId", condition.getNodeId());
    }

    @Test
    public void GameNode_Condition_EqualsShouldWorkProperly() {
        EqualsVerifier.forClass(GameNode.Condition.class)
                .usingGetClass()
                .withNonnullFields("flagId", "flagState", "nodeId")
                .verify();
    }
}