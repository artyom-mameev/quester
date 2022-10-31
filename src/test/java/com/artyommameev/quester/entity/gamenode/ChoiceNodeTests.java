package com.artyommameev.quester.entity.gamenode;

import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ChoiceNodeTests {

    @Test(expected = GameNode.EmptyStringException.class)
    public void constructorThrowsEmptyStringExceptionIfIdIsEmpty() throws Exception {
        new ChoiceNode("", "testName");
    }

    @Test(expected = GameNode.EmptyStringException.class)
    public void constructorThrowsEmptyStringExceptionIfNameIsEmpty() throws Exception {
        new ChoiceNode("id", "");
    }

    @Test(expected = GameNode.NullValueException.class)
    public void constructorThrowsNullValueExceptionIfIdIsNull() throws Exception {
        new ChoiceNode(null, "testName");
    }

    @Test(expected = GameNode.NullValueException.class)
    public void constructorThrowsNullValueExceptionIfNameIsNull() throws Exception {
        new ChoiceNode("id", null);
    }

    @Test
    public void constructorProperlyConstructs() throws Exception {
        val choiceNode = new ChoiceNode("id", "testName");

        assertEquals(choiceNode.getId(), "id");
        assertEquals(choiceNode.getName(), "testName");
        assertEquals(choiceNode.getType(), GameNode.NodeType.CHOICE);
    }
}
