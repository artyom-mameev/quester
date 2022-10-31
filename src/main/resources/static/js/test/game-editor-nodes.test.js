/**
 * @jest-environment jsdom
 */

const gameEditor_NodesModule = require("../game-editor-nodes");

beforeAll(() => {
    setUpJquery();
    setUpGlobalVariables();
});

function setUpJquery() {
    global.$ = require("jquery");
    global.jQuery = global.$;
}

function setUpGlobalVariables() {
    global.DESC_INDEX = 0;
    global.COND_INDEX = 1;

    global.IF_STRING = "IF_STRING";
    global.IS_ACTIVE_STRING = "IS_ACTIVE_STRING";
    global.IS_NOT_ACTIVE_STRING = "IS_NOT_ACTIVE_STRING";

    global.FLAG_STATE = {
        FLAG_ACTIVE: "ACTIVE",
        FLAG_NOT_ACTIVE: "NOT_ACTIVE"
    }
    global.NODE_TYPE = {
        ROOM: "ROOM",
        CHOICE: "CHOICE",
        FLAG: "FLAG",
        CONDITION: "CONDITION"
    }
}

test('GameNode constructs correct GameNode object', () => {
    const node = new gameEditor_NodesModule.GameNode("testName",
        "testDesc", "testType");

    expect(node.text).toEqual("testName");
    expect(node.data[DESC_INDEX]).toEqual("testDesc");
    expect(node.type).toEqual("testType");
});

test('Condition constructs correct Condition object', () => {
    const condition = new gameEditor_NodesModule.Condition("testFlagId",
        "testFlagState");

    expect(condition.flagId).toEqual("testFlagId");
    expect(condition.flagState).toEqual("testFlagState");
});

test('convertToJstreeNodesFormat converts game nodes to jstree nodes format', () => {
    const node = {
        name: "testName",
        type: NODE_TYPE.ROOM,
        description: "testDesc",
        children: [{
            type: NODE_TYPE.CHOICE,
            children: [{
                name: "testName2",
                type: NODE_TYPE.ROOM,
                description: "testDesc2",
                children: [{
                    type: NODE_TYPE.FLAG,
                    children: [{
                        type: NODE_TYPE.CONDITION,
                        condition: "testCondition",
                        children: []
                    }]
                }]
            }]
        }]
    }

    gameEditor_NodesModule.convertToJstreeNodesFormat(node);

    expect(node.text).toEqual("testName");
    expect(node.data[DESC_INDEX]).toEqual("testDesc");
    expect(node.children[0].children[0].text).toEqual("testName2");
    expect(node.children[0].children[0].data[DESC_INDEX])
        .toEqual("testDesc2");
    expect(node.children[0].children[0].children[0].children[0].data[COND_INDEX])
        .toBe("testCondition");
});

test('createNamesForConditionNodes creates names for condition nodes', () => {
    const node = {
        name: "testName",
        type: NODE_TYPE.ROOM,
        description: "testDesc",
        children: [
            {
                id: "testFlagId",
                text: "testFlagText",
                type: NODE_TYPE.FLAG,
                children: []
            },
            {
                id: "testFlagId2",
                text: "testFlagText2",
                type: NODE_TYPE.FLAG,
                children: []
            },
            {
                type: NODE_TYPE.CONDITION,
                data: [null, {
                    flagId: "testFlagId",
                    flagState: FLAG_STATE.FLAG_ACTIVE
                }],
                children: []
            },
            {
                type: NODE_TYPE.CONDITION,
                data: [null, {
                    flagId: "testFlagId2",
                    flagState: FLAG_STATE.FLAG_NOT_ACTIVE
                }],
                children: []
            }
        ]
    }

    gameEditor_NodesModule.createNamesForConditionNodes(node);

    expect(node.children[2].text).toEqual(IF_STRING + " testFlagText " +
        IS_ACTIVE_STRING);
    expect(node.children[3].text).toEqual(IF_STRING + " testFlagText2 " +
        IS_NOT_ACTIVE_STRING);
});

test('createConditionName creates condition name for active flag state', () => {
    const conditionName = gameEditor_NodesModule.createConditionName(
        {text: "testFlagText"}, FLAG_STATE.FLAG_ACTIVE);

    expect(conditionName).toEqual(IF_STRING + " testFlagText " +
        IS_ACTIVE_STRING);
});

test('createConditionName creates condition name for not active flag state', () => {
    const conditionName = gameEditor_NodesModule.createConditionName(
        {text: "testFlagText"}, FLAG_STATE.FLAG_NOT_ACTIVE);

    expect(conditionName).toEqual(IF_STRING + " testFlagText " +
        IS_NOT_ACTIVE_STRING);
});

test('getAllFlags returns all FLAG nodes', () => {
    const flag1 = {
        id: "testFlagId",
        text: "testFlagText",
        type: NODE_TYPE.FLAG,
        children: []
    }

    const flag2 = {
        id: "testFlagId2",
        text: "testFlagText2",
        type: NODE_TYPE.FLAG,
        children: []
    }

    const node = {
        type: NODE_TYPE.ROOM,
        children: [
            flag1, {
                type: NODE_TYPE.CHOICE,
                children: [{
                    type: NODE_TYPE.ROOM,
                    children: [flag2, {
                        type: NODE_TYPE.CONDITION,
                        children: []
                    }]
                }
                ]
            }
        ]
    }

    const flags = gameEditor_NodesModule.getAllFlags(node);

    expect(flags.length).toBe(2);
    expect(flags[0]).toBe(flag1);
    expect(flags[1]).toBe(flag2);
});

test('getAllConditions returns all conditions', () => {
    const condition1 = {
        flagId: "testFlagId",
        flagState: "testFlagState",
    }

    const condition2 = {
        flagId: "testFlagId2",
        flagState: "testFlagState2",
    }

    const node = {
        type: NODE_TYPE.ROOM,
        children: [
            {
                type: NODE_TYPE.FLAG,
                children: []
            },
            {
                type: NODE_TYPE.CHOICE,
                children: [
                    {
                        type: NODE_TYPE.CONDITION,
                        data: [null, condition1],
                        children: []
                    },
                    {
                        type: NODE_TYPE.ROOM,
                        children: [
                            {
                                type: NODE_TYPE.CONDITION,
                                data: [null, condition2],
                                children: []
                            }
                        ]
                    }]
            }
        ]
    }

    const conditions = gameEditor_NodesModule.getAllConditions(node);

    expect(conditions.length).toBe(2);
    expect(conditions[0]).toBe(condition1);
    expect(conditions[1]).toBe(condition2);
});
