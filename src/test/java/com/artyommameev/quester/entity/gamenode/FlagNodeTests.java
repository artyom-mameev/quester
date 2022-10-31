package com.artyommameev.quester.entity.gamenode;

import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FlagNodeTests {

    @Test(expected = GameNode.EmptyStringException.class)
    public void constructorThrowsEmptyStringExceptionIfIdIsEmpty() throws Exception {
        new FlagNode("", "testName");
    }

    @Test(expected = GameNode.EmptyStringException.class)
    public void constructorThrowsEmptyStringExceptionIfNameIsEmpty() throws Exception {
        new FlagNode("id", "");
    }

    @Test(expected = GameNode.NullValueException.class)
    public void constructorThrowsNullValueExceptionIfIdIsNull() throws Exception {
        new FlagNode(null, "testName");
    }

    @Test(expected = GameNode.NullValueException.class)
    public void constructorThrowsNullValueExceptionIfNameIsNull() throws Exception {
        new FlagNode("id", null);
    }

    @Test
    public void constructorProperlyConstructs() throws Exception {
        val flagNode = new FlagNode("id", "testName");

        assertEquals(flagNode.getId(), "id");
        assertEquals(flagNode.getName(), "testName");
        assertEquals(flagNode.getType(), GameNode.NodeType.FLAG);
    }
}
