package com.artyommameev.quester.entity.gamenode;

import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConditionNodeTests {

    @Test(expected = GameNode.EmptyStringException.class)
    public void constructorThrowsEmptyStringExceptionIfIdIsEmpty() throws Exception {
        new ConditionNode("", "testFlagId",
                GameNode.Condition.FlagState.ACTIVE);
    }

    @Test(expected = GameNode.EmptyStringException.class)
    public void constructorThrowsEmptyStringExceptionIfConditionFlagIdIsEmpty() throws Exception {
        new ConditionNode("id", "",
                GameNode.Condition.FlagState.ACTIVE);
    }

    @Test(expected = GameNode.NullValueException.class)
    public void constructorThrowsNullValueExceptionIfIdIsNull() throws Exception {
        new ConditionNode(null, "testFlagId",
                GameNode.Condition.FlagState.ACTIVE);
    }

    @Test(expected = GameNode.NullValueException.class)
    public void constructorThrowsNullValueExceptionConditionFlagIdIsNull() throws Exception {
        new ConditionNode("id", null,
                GameNode.Condition.FlagState.ACTIVE);
    }

    @Test(expected = GameNode.NullValueException.class)
    public void constructorThrowsNullValueExceptionConditionFlagStateIsNull() throws Exception {
        new ConditionNode("id", "testFlagId", null);
    }

    @Test
    public void constructorProperlyConstructs() throws Exception {
        val conditionNode = new ConditionNode("id", "testFlagId",
                GameNode.Condition.FlagState.ACTIVE);

        assertEquals(conditionNode.getId(), "id");
        assertEquals(conditionNode.getCondition().getNodeId(),
                "id");
        assertEquals(conditionNode.getCondition().getFlagId(),
                "testFlagId");
        assertEquals(conditionNode.getCondition().getFlagState(),
                GameNode.Condition.FlagState.ACTIVE);
        assertEquals(conditionNode.getType(), GameNode.NodeType.CONDITION);
    }
}
