package com.artyommameev.quester.entity.gamenode;

import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RoomNodeTests {

    @Test(expected = GameNode.EmptyStringException.class)
    public void constructorThrowsEmptyStringExceptionIfIdIsEmpty() throws Exception {
        new RoomNode("", "testName", "testDesc");
    }

    @Test(expected = GameNode.EmptyStringException.class)
    public void constructorThrowsEmptyStringExceptionIfNameIsEmpty() throws Exception {
        new RoomNode("id", "", "testDesc");
    }

    @Test(expected = GameNode.EmptyStringException.class)
    public void constructorThrowsEmptyStringExceptionIfDescriptionIsEmpty() throws Exception {
        new RoomNode("id", "testName", "");
    }

    @Test(expected = GameNode.NullValueException.class)
    public void constructorThrowsNullValueExceptionIfIdIsNull() throws Exception {
        new RoomNode(null, "testName", "testDesc");
    }

    @Test(expected = GameNode.NullValueException.class)
    public void constructorThrowsNullValueExceptionIfNameIsNull() throws Exception {
        new RoomNode("id", null, "testDesc");
    }

    @Test(expected = GameNode.NullValueException.class)
    public void constructorThrowsNullValueExceptionIfDescriptionIsNull() throws Exception {
        new RoomNode("id", "testName", null);
    }

    @Test
    public void constructorProperlyConstructs() throws Exception {
        val roomNode = new RoomNode("id", "testName",
                "testDesc");

        assertEquals(roomNode.getId(), "id");
        assertEquals(roomNode.getName(), "testName");
        assertEquals(roomNode.getDescription(), "testDesc");
        assertEquals(roomNode.getType(), GameNode.NodeType.ROOM);
    }
}
