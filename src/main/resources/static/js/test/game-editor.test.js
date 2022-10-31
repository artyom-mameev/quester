/**
 * @jest-environment jsdom
 */

const gameEditorModule = require("../game-editor");
const testUtils = require("./util/test-utils.js")

beforeAll(() => {
    setUpJquery();
    setUpGlobalVariables();
});

beforeEach(() => {
    setUpMocks();
    setUpTestData();
});

function setUpJquery() {
    global.$ = require("jquery");
    global.jQuery = global.$;
}

function setUpGlobalVariables() {
    global.DESC_INDEX = 0;
    global.COND_INDEX = 1;

    global.ADD_ROOM_STRING = "ADD_ROOM_STRING";
    global.ADD_CHOICE_STRING = "ADD_CHOICE_STRING";
    global.ADD_CONDITION_STRING = "ADD_CONDITION_STRING";
    global.EDIT_ITEM_STRING = "EDIT_ITEM_STRING";
    global.DELETE_ITEM_STRING = "DELETE_ITEM_STRING";
    global.ARE_YOU_SURE_STRING = "ARE_YOU_SURE_STRING";
    global.FLAG_DELETE_ALERT_STRING = "FLAG_DELETE_ALERT_STRING";
    global.IF_STRING = "IF_STRING";
    global.IS_ACTIVE_STRING = "IS_ACTIVE_STRING";
    global.IS_NOT_ACTIVE_STRING = "IS_NOT_ACTIVE_STRING";
    global.ROOM_NAME_STRING = "ROOM_NAME_STRING";
    global.ROOM_DESC_STRING = "ROOM_DESC_STRING";
    global.CHOICE_NAME_STRING = "CHOICE_NAME_STRING";
    global.CAN_NOT_DELETE_ITEM_STRING = "CAN_NOT_DELETE_ITEM_STRING";

    global.FLAG_STATE = {
        FLAG_ACTIVE: "ACTIVE",
        FLAG_NOT_ACTIVE: "NOT_ACTIVE"
    }
    global.ALERT_MODE = {
        INFO: "INFO",
        QUESTION: "QUESTION"
    };
    global.HTTP_REQUEST_TYPE = {
        POST: "POST",
        PUT: "PUT",
        DELETE: "DELETE",
    }
    global.NODE_TYPE = {
        ROOM: "ROOM",
        CHOICE: "CHOICE",
        FLAG: "FLAG",
        CONDITION: "CONDITION"
    }
}

function setUpTestData() {
    global.testGameToEditWithNullRootNode = {
        id: 1,
        name: "testGameName",
        description: "testGameDescription",
        language: "testGameLanguage",
        rootNode: null
    }

    global.testGameToEditWithNotNullRootNode = {
        id: 1,
        name: "testGameName",
        description: "testGameDescription",
        language: "testGameLanguage",
        rootNode: {
            type: NODE_TYPE.ROOM,
            id: "testRootNodeId",
            name: "testRootNodeName",
            description: "testRootNodeDescription",
            condition: null,
            children: [
                {
                    type: NODE_TYPE.CHOICE,
                    id: "testChoiceNodeId",
                    name: "testChoiceNodeName",
                    description: null,
                    condition: null,
                    children: [{
                        type: NODE_TYPE.CONDITION,
                        id: "testConditionNodeId",
                        name: null,
                        description: null,
                        condition: {
                            flagId: "testFlagNodeId",
                            flagState: FLAG_STATE.FLAG_ACTIVE,
                            nodeId: "testConditionNodeId"
                        },
                        children: [],
                    }],
                },
                {
                    type: NODE_TYPE.FLAG,
                    id: "testFlagNodeId",
                    name: "testFlagNodeName",
                    description: null,
                    condition: null,
                    children: []
                }],
        }
    }

    global.rootNode = {
        type: NODE_TYPE.ROOM,
        parent: '#',
        id: "testRootNodeId",
        text: "testRootNodeName",
        data: ["testRootNodeDescription", null],
        children: [],
    }

    global.roomNode = {
        type: NODE_TYPE.ROOM,
        parent: "testRoomNodeParent",
        id: "testRoomNodeId",
        text: "testRoomNodeName",
        data: ["testRoomNodeDescription", null],
        children: [],
    }

    global.choiceNode = {
        type: NODE_TYPE.CHOICE,
        parent: "testChoiceNodeParent",
        id: "testChoiceNodeId",
        text: "testChoiceNodeName",
        data: [null, null],
        children: ["childId"],
    }

    global.flagNode = {
        type: NODE_TYPE.FLAG,
        parent: "testFlagNodeParent",
        id: "testFlagNodeId",
        text: "testFlagNodeName",
        data: [null, null],
        children: [],
    }

    global.conditionNode = {
        type: NODE_TYPE.CONDITION,
        parent: "testConditionNodeParent",
        id: "testConditionNodeId",
        text: "testConditionNodeName",
        data: [null, {}],
        children: ["childId"],
    }

    global.otherTypeNode = {
        type: "otherType",
        parent: 'otherParent',
        id: "otherId",
        data: ["otherDescription", {}],
        children: [],
    }

    global.placeholderFlag = {
        type: null,
        parent: null,
        id: 1,
        data: [null, null],
        children: [],
    }

    global.testIconsUrl = {
        roomIcon: "ROOM_ICON",
        choiceIcon: "CHOICE_ICON",
        flagIcon: "FLAG_ICON",
        conditionIcon: "CONDITION_ICON"
    }

    class GameNode {
        data = [];

        constructor(name, description, type) {
            this.text = name;
            this.data.push(description);
            this.type = type;
        }
    }

    global.GameNode = GameNode;

    class Condition {
        constructor(flagId, flagState) {
            this.flagId = flagId;
            this.flagState = flagState;
        }
    }

    global.Condition = Condition;
}

function setUpMocks() {
    mockJstree();

    testUtils.mockVar("Option", window.Option);

    testUtils.mockFuncs("convertToJstreeNodesFormat",
        "createNamesForConditionNodes", "showEditInfoButton",
        "showModal", "prepareModalForAddingRoomNode",
        "setModalSaveButtonOnClick", "clearErrorsInModal",
        "getNameValueFromModal", "getDescValueFromModal",
        "makeAjaxRequest", "showModalFieldErrors",
        "hideModal", "prepareModalForAddingChoiceNode", "isModalCheckboxChecked",
        "prepareModalForAddingConditionNode", "getSelectValueFromModal",
        "getModalRadioValue", "createConditionName",
        "prepareModalForEditingRoomNode", "prepareModalForEditingChoiceNode",
        "prepareModalForEditingConditionNode", "showAlertModal",
        "hideEditInfoButton", "preventClosingModal",
        "prepareModalForCreatingGame", "prepareModalForUpdatingGameInfo",
        "showPlayButton", "hidePlayButton");

    testUtils.mockImpl("getAllFlags", function () {
        return [];
    });

    testUtils.mockImpl("getAllConditions", function () {
        return [];
    });
}

test('initGameEditor converts nodes to jstree nodes format if root node is' +
    ' not null', () => {
    testUtils.mockImpl("convertToJstreeNodesFormat", function (node) {
        require("../game-editor-nodes").convertToJstreeNodesFormat(node);
    });

    const oldRootNode = JSON.parse(JSON.stringify( //clone object
        testGameToEditWithNotNullRootNode.rootNode));

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const jstreeMockCall = $().jstree.mock.calls[0][0];

    const rootNode = jstreeMockCall.core.data;

    require("../game-editor-nodes").convertToJstreeNodesFormat(oldRootNode);

    expect(rootNode).toEqual(oldRootNode);
});

test('initGameEditor creates names for condition nodes if root node' +
    ' is not null', () => {
    testUtils.mockImpl("convertToJstreeNodesFormat", function (node) {
        require("../game-editor-nodes").convertToJstreeNodesFormat(node);
    });
    testUtils.mockImpl("createNamesForConditionNodes",
        function (node, rootNode) {
            require("../game-editor-nodes")
                .createNamesForConditionNodes(node, rootNode);
        });

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const jstreeMockCall = $().jstree.mock.calls[0][0];

    const rootNode = jstreeMockCall.core.data;

    expect(rootNode.children[0].children[0].text)
        .toEqual("IF_STRING testFlagNodeName IS_ACTIVE_STRING");
});

test('initGameEditor initializes jstree', () => {
    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    expect($().jstree).toHaveBeenCalledTimes(2);

    const jstreeMockCall = $().jstree.mock.calls[0][0];

    expect(jstreeMockCall.core.check_callback()).toBeTruthy();
    expect(jstreeMockCall.types['#'].max_children).toBe(1);
    expect(jstreeMockCall.types.ROOM.icon).toBe("ROOM_ICON");
    expect(jstreeMockCall.types.CHOICE.icon).toBe("CHOICE_ICON");
    expect(jstreeMockCall.types.FLAG.icon).toBe("FLAG_ICON");
    expect(jstreeMockCall.types.CONDITION.icon).toBe("CONDITION_ICON");
    expect(jstreeMockCall.plugins.includes('contextmenu'))
        .toBe(true);
    expect(jstreeMockCall.plugins.includes('state'))
        .toBe(true);
    expect(jstreeMockCall.plugins.includes('types'))
        .toBe(true);
    expect(jstreeMockCall.plugins.includes('wholerow'))
        .toBe(true);
    expect(jstreeMockCall.plugins.includes('sort'))
        .toBe(true);
});

test('initGameEditor opens all nodes on loaded jstree', () => {
    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const jstreeMockCall = $().jstree.mock.calls[1][0];

    expect(jstreeMockCall).toBe("open_all");
});

test('initGameEditor sets jstree to sort ROOM nodes after CONDITION nodes', () => {
    const conditionNode = {
        type: NODE_TYPE.CONDITION
    }
    const roomNode = {
        type: NODE_TYPE.ROOM
    }

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const jstreeMockCall = $().jstree.mock.calls[0][0];

    jstreeMockCall.get_node = jest.fn()
        .mockImplementation(function (node) {
            return node;
        });

    expect(jstreeMockCall.sort(conditionNode, roomNode)).toBe(-1);
});

test('initGameEditor sets labels for context menu', () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(otherTypeNode);

    expect(menuItems.addRoom.label).toBe(ADD_ROOM_STRING);
    expect(menuItems.addChoice.label).toBe(ADD_CHOICE_STRING);
    expect(menuItems.addCondition.label).toBe(ADD_CONDITION_STRING);
    expect(menuItems.editItem.label).toBe(EDIT_ITEM_STRING);
    expect(menuItems.deleteItem.label).toBe(DELETE_ITEM_STRING);
});

test('initGameEditor sets correct context menu for root node', () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(rootNode);

    expect(menuItems.addRoom).toBeUndefined();
    expect(menuItems.addChoice).not.toBeUndefined();
    expect(menuItems.addCondition).toBeUndefined();
    expect(menuItems.editItem).not.toBeUndefined();
    expect(menuItems.deleteItem).toBeUndefined();
});

test('initGameEditor sets correct context menu for ROOM node', () => {
    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(roomNode);

    expect(menuItems.addRoom).toBeUndefined();
    expect(menuItems.addChoice).not.toBeUndefined();
    expect(menuItems.addCondition).toBeUndefined();
    expect(menuItems.editItem).not.toBeUndefined();
    expect(menuItems.deleteItem).not.toBeUndefined();
});

test('initGameEditor sets correct context menu for CHOICE node' +
    ' if room exists among children and at least 1 FLAG node exists', () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });
    mockJstree_getNodeReturnsRoomNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    expect(menuItems.addRoom).toBeUndefined();
    expect(menuItems.addChoice).toBeUndefined();
    expect(menuItems.addCondition).not.toBeUndefined();
    expect(menuItems.editItem).not.toBeUndefined();
    expect(menuItems.deleteItem).not.toBeUndefined();
});

test('initGameEditor sets correct context menu for CHOICE node' +
    " if room exists among node's children and FLAG nodes are not exists", () => {
    mockJstree_getNodeReturnsRoomNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    expect(menuItems.addRoom).toBeUndefined();
    expect(menuItems.addChoice).toBeUndefined();
    expect(menuItems.addCondition).toBeUndefined();
    expect(menuItems.editItem).not.toBeUndefined();
    expect(menuItems.deleteItem).not.toBeUndefined();
});

test('initGameEditor sets correct context menu for CHOICE node' +
    " if room not exists among node's children and at least 1 FLAG node exists", () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });
    mockJstree_getNodeReturnsChoiceNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    expect(menuItems.addRoom).not.toBeUndefined();
    expect(menuItems.addChoice).toBeUndefined();
    expect(menuItems.addCondition).not.toBeUndefined();
    expect(menuItems.editItem).not.toBeUndefined();
    expect(menuItems.deleteItem).not.toBeUndefined();
});

test('initGameEditor sets correct context menu for CHOICE node' +
    " if room not exists among node's children and FLAG nodes are not exists", () => {
    mockJstree_getNodeReturnsChoiceNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    expect(menuItems.addRoom).not.toBeUndefined();
    expect(menuItems.addChoice).toBeUndefined();
    expect(menuItems.addCondition).toBeUndefined();
    expect(menuItems.editItem).not.toBeUndefined();
    expect(menuItems.deleteItem).not.toBeUndefined();
});

test('initGameEditor sets correct context menu for CONDITION node' +
    " if room exists among node's children and at least 1 FLAG node exists", () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });
    mockJstree_getNodeReturnsRoomNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(conditionNode);

    expect(menuItems.addRoom).toBeUndefined();
    expect(menuItems.addChoice).toBeUndefined();
    expect(menuItems.addCondition).not.toBeUndefined();
    expect(menuItems.editItem).not.toBeUndefined();
    expect(menuItems.deleteItem).not.toBeUndefined();
});

test('initGameEditor sets correct context menu for CONDITION node' +
    " if room exists among node's children and FLAG nodes are not exists", () => {
    mockJstree_getNodeReturnsRoomNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(conditionNode);

    expect(menuItems.addRoom).toBeUndefined();
    expect(menuItems.addChoice).toBeUndefined();
    expect(menuItems.addCondition).toBeUndefined();
    expect(menuItems.editItem).not.toBeUndefined();
    expect(menuItems.deleteItem).not.toBeUndefined();
});

test('initGameEditor sets correct context menu for CONDITION node' +
    " if room not exists among node's children and at least 1 FLAG node exists", () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });
    mockJstree_getNodeReturnsChoiceNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(conditionNode);

    expect(menuItems.addRoom).not.toBeUndefined();
    expect(menuItems.addChoice).toBeUndefined();
    expect(menuItems.addCondition).not.toBeUndefined();
    expect(menuItems.editItem).not.toBeUndefined();
    expect(menuItems.deleteItem).not.toBeUndefined();
});

test('initGameEditor sets correct context menu for CONDITION node' +
    " if room not exists among node's children and FLAG nodes are not exists", () => {
    mockJstree_getNodeReturnsChoiceNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(conditionNode);

    expect(menuItems.addRoom).not.toBeUndefined();
    expect(menuItems.addChoice).toBeUndefined();
    expect(menuItems.addCondition).toBeUndefined();
    expect(menuItems.editItem).not.toBeUndefined();
    expect(menuItems.deleteItem).not.toBeUndefined();
});

test('initGameEditor sets correct context menu for FLAG node', () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(flagNode);

    expect(menuItems.addRoom).toBeUndefined();
    expect(menuItems.addChoice).toBeUndefined();
    expect(menuItems.addCondition).toBeUndefined();
    expect(menuItems.editItem).not.toBeUndefined();
    expect(menuItems.deleteItem).not.toBeUndefined();
});

test('initGameEditor sets and shows add room modal', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    mockJstree_getNodeReturnsChoiceNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    menuItems.addRoom.action();

    expect(testUtils.getVar("showModal"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("prepareModalForAddingRoomNode"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("setModalSaveButtonOnClick"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor sets add room modal that on save button click' +
    ' errors should be cleared', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    mockJstree_getNodeReturnsChoiceNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    menuItems.addRoom.action();

    expect(testUtils.getVar("clearErrorsInModal"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor sets add room modal that on save button click' +
    ' new jstree node should be created', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("getNameValueFromModal",
        function () {
            return "testName";
        });
    testUtils.mockImpl("getDescValueFromModal",
        function () {
            return "testDesc";
        });

    const jstreeMock = mockJstree_getNodeReturnsChoiceNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    menuItems.addRoom.action();

    expect(jstreeMock.create_node).toHaveBeenCalledTimes(1);
    expect(jstreeMock.create_node).toHaveBeenCalledWith(
        "testChoiceNodeId", {
            data: ["testDesc"], text: "testName",
            type: NODE_TYPE.ROOM
        });
});


test('initGameEditor sets add room modal that on save button click' +
    ' POST request to server should be created', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("getNameValueFromModal",
        function () {
            return "testName";
        });
    testUtils.mockImpl("getDescValueFromModal",
        function () {
            return "testDesc";
        });
    mockJstree_getNodeReturnsChoiceNode_createNodeReturnsCreatedId();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    menuItems.addRoom.action();

    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("makeAjaxRequest")).toHaveBeenCalledWith(
        HTTP_REQUEST_TYPE.POST, "/api/games/1/nodes/", {
            id: "createdId", parentId: "testChoiceNodeId", name: "testName",
            description: "testDesc", type: NODE_TYPE.ROOM, condition: undefined
        }, expect.any(Function));
});

test('initGameEditor sets add room modal that on save button click' +
    ' after failure POST request errors should be shown', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithFieldError);
    mockJstree_getNodeReturnsChoiceNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    menuItems.addRoom.action();

    expect(testUtils.getVar("showModalFieldErrors"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showModalFieldErrors"))
        .toHaveBeenCalledWith(
            [{defaultMessage: "TestError"}]);
});

test('initGameEditor sets add room modal that on save button click' +
    ' after failure POST request added node should be deleted', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithFieldError);
    const jstreeMock =
        mockJstree_getNodeReturnsChoiceNode_createNodeReturnsCreatedId();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    menuItems.addRoom.action();

    expect(jstreeMock.delete_node).toHaveBeenCalledTimes(1);
    expect(jstreeMock.delete_node).toHaveBeenCalledWith("createdId");
});

test('initGameEditor sets add room modal that on save button click' +
    ' after success POST request modal should be hidden', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithoutFieldError);
    mockJstree_getNodeReturnsChoiceNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    menuItems.addRoom.action();

    expect(testUtils.getVar("hideModal"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor sets add room modal that on save button click' +
    ' after success POST request all jstree nodes should be open', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithoutFieldError);
    const jstreeMock =
        mockJstree_getNodeReturnsChoiceNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    menuItems.addRoom.action();

    expect(jstreeMock.open_all).toHaveBeenCalledTimes(1);
    expect(jstreeMock.open_all).toHaveBeenCalledWith(
        "testChoiceNodeId");
});

test('initGameEditor sets and shows add choice modal', () => {
    testUtils.mockImpl("showModal", justRunsFunc);

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(roomNode);

    menuItems.addChoice.action();

    expect(testUtils.getVar("showModal"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("prepareModalForAddingChoiceNode"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("setModalSaveButtonOnClick"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor sets add choice modal that on save button click' +
    ' errors should be cleared', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(roomNode);

    menuItems.addChoice.action();

    expect(testUtils.getVar("clearErrorsInModal"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor sets add choice modal that on save button click' +
    ' new CHOICE node should be created if modal checkbox is not checked', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("getNameValueFromModal",
        function () {
            return "testName";
        });
    testUtils.mockImpl("isModalCheckboxChecked",
        function () {
            return false;
        });

    const jstreeMock = mockJstree();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(roomNode);

    menuItems.addChoice.action();

    expect(jstreeMock.create_node).toHaveBeenCalledTimes(1);
    expect(jstreeMock.create_node).toHaveBeenCalledWith(
        "testRoomNodeId", {
            data: [null], text: "testName",
            type: NODE_TYPE.CHOICE
        });
});

test('initGameEditor sets add choice modal that on save button click' +
    'new FLAG jstree node should be created if modal checkbox is checked', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("getNameValueFromModal",
        function () {
            return "testName";
        });
    testUtils.mockImpl("isModalCheckboxChecked",
        function () {
            return true;
        });

    const jstreeMock = mockJstree();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(roomNode);

    menuItems.addChoice.action();

    expect(jstreeMock.create_node).toHaveBeenCalledTimes(1);
    expect(jstreeMock.create_node).toHaveBeenCalledWith(
        "testRoomNodeId", {
            data: [null], text: "testName",
            type: NODE_TYPE.FLAG
        });
});

test('initGameEditor sets add choice modal that on save button click' +
    ' POST request with CHOICE node should be created if modal checkbox' +
    ' is not checked', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("getNameValueFromModal",
        function () {
            return "testName";
        });
    testUtils.mockImpl("isModalCheckboxChecked",
        function () {
            return false;
        });

    mockJstree_createNodeReturnsCreatedId();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(roomNode);

    menuItems.addChoice.action();

    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("makeAjaxRequest")).toHaveBeenCalledWith(
        HTTP_REQUEST_TYPE.POST, "/api/games/1/nodes/", {
            id: "createdId", parentId: "testRoomNodeId", name: "testName",
            description: null, type: NODE_TYPE.CHOICE, condition: undefined
        }, expect.any(Function));
});

test('initGameEditor sets add choice modal that on save button click' +
    ' POST request with FLAG node should be created if modal checkbox is checked', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("getNameValueFromModal",
        function () {
            return "testName";
        });
    testUtils.mockImpl("isModalCheckboxChecked",
        function () {
            return true;
        });

    mockJstree_createNodeReturnsCreatedId();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(roomNode);

    menuItems.addChoice.action();

    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("makeAjaxRequest")).toHaveBeenCalledWith(
        HTTP_REQUEST_TYPE.POST, "/api/games/1/nodes/", {
            id: "createdId", parentId: "testRoomNodeId", name: "testName",
            description: null, type: NODE_TYPE.FLAG, condition: undefined
        }, expect.any(Function));
});

test('initGameEditor sets add choice modal that on save button click' +
    ' after failure POST request errors should be shown', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithFieldError);

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(roomNode);

    menuItems.addChoice.action();

    expect(testUtils.getVar("showModalFieldErrors"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showModalFieldErrors"))
        .toHaveBeenCalledWith([{defaultMessage: "TestError"}]);
});

test('initGameEditor sets add choice modal that on save button click' +
    ' after failure POST request added node should be deleted', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithFieldError);
    const jstreeMock = mockJstree_createNodeReturnsCreatedId();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(roomNode);

    menuItems.addChoice.action();

    expect(jstreeMock.delete_node).toHaveBeenCalledTimes(1);
    expect(jstreeMock.delete_node).toHaveBeenCalledWith("createdId");
});

test('initGameEditor sets add choice modal that on save button click' +
    ' after successful addition of FLAG node, that FLAG node should appears' +
    ' when trying to add CONDITION node', () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithoutFieldError);
    testUtils.mockImpl("getNameValueFromModal",
        function () {
            return "testName";
        });
    testUtils.mockImpl("isModalCheckboxChecked",
        function () {
            return true;
        });
    mockJstree_getNodeReturnsChoiceNode_createNodeReturnsCreatedId();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItemsRoom = getJstreeContextMenuItemsFor(roomNode);

    menuItemsRoom.addChoice.action();

    const menuItemsChoice = getJstreeContextMenuItemsFor(choiceNode);

    menuItemsChoice.addCondition.action();

    const prepareModalForAddingConditionNodeCall =
        prepareModalForAddingConditionNode.mock.calls[0][0];

    expect(prepareModalForAddingConditionNodeCall[1]).toEqual({
        data: [null],
        id: "createdId",
        text: "testName",
        type: NODE_TYPE.FLAG,
    });
});

test('initGameEditor sets add choice modal that on save button click' +
    ' after successful POST request modal should be hidden', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithoutFieldError);

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(roomNode);

    menuItems.addChoice.action();

    expect(testUtils.getVar("hideModal"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor sets add choice modal that on save button click' +
    ' after successful POST request jstree should be open', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithoutFieldError);
    const jstreeMock = mockJstree();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(roomNode);

    menuItems.addChoice.action();

    expect(jstreeMock.open_all).toHaveBeenCalledTimes(1);
    expect(jstreeMock.open_all).toHaveBeenCalledWith("testRoomNodeId");
});

test('initGameEditor sets and shows add condition modal', () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });
    testUtils.mockImpl("showModal", justRunsFunc);
    mockJstree_getNodeReturnsRoomNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    menuItems.addCondition.action();

    expect(testUtils.getVar("showModal"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("prepareModalForAddingConditionNode"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("setModalSaveButtonOnClick"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor sets add condition modal that on save button click' +
    ' errors should be cleared', () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    mockJstree_getNodeReturnsChoiceNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    menuItems.addCondition.action();

    expect(testUtils.getVar("clearErrorsInModal"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor sets add condition modal that on save button click' +
    ' jstree node should be created', () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("createConditionName",
        function () {
            return "conditionName";
        });
    testUtils.mockImpl("getModalRadioValue", function () {
        return "testRadioValue";
    });
    testUtils.mockImpl("getSelectValueFromModal", function () {
        return "testFlagNodeId";
    });
    testUtils.mockImpl("getModalRadioValue", function () {
        return FLAG_STATE.FLAG_ACTIVE;
    });
    const jstreeMock = mockJstree_getNodeReturnsChoiceNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    menuItems.addCondition.action();

    expect(jstreeMock.create_node).toHaveBeenCalledTimes(1);
    expect(jstreeMock.create_node).toHaveBeenCalledWith(
        "testChoiceNodeId", {
            data: [null, {
                flagId: "testFlagNodeId",
                flagState: FLAG_STATE.FLAG_ACTIVE
            }], text: "conditionName",
            type: NODE_TYPE.CONDITION
        });
});

test('initGameEditor sets add condition modal that on save button click' +
    ' POST request should be created with active flag status if radio value' +
    ' is active', () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("createConditionName",
        function () {
            return "conditionName";
        });
    testUtils.mockImpl("getModalRadioValue", function () {
        return "testRadioValue";
    });
    testUtils.mockImpl("getSelectValueFromModal", function () {
        return "testFlagNodeId";
    });
    testUtils.mockImpl("getModalRadioValue", function () {
        return FLAG_STATE.FLAG_ACTIVE;
    });
    mockJstree_getNodeReturnsChoiceNode_createNodeReturnsCreatedId();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    menuItems.addCondition.action();

    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("makeAjaxRequest")).toHaveBeenCalledWith(
        HTTP_REQUEST_TYPE.POST, "/api/games/1/nodes/", {
            id: "createdId", parentId: "testChoiceNodeId", name: null,
            description: null, type: NODE_TYPE.CONDITION, condition:
                {flagId: "testFlagNodeId", flagState: "ACTIVE"}
        }, expect.any(Function));
});

test('initGameEditor sets add condition modal that on save button click' +
    ' POST request should be created with not active flag status if radio value' +
    ' is not active', () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("createConditionName",
        function () {
            return "conditionName";
        });
    testUtils.mockImpl("getModalRadioValue", function () {
        return "testRadioValue";
    });
    testUtils.mockImpl("getSelectValueFromModal", function () {
        return "testFlagNodeId";
    });
    testUtils.mockImpl("getModalRadioValue", function () {
        return FLAG_STATE.FLAG_NOT_ACTIVE;
    });
    mockJstree_getNodeReturnsChoiceNode_createNodeReturnsCreatedId();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    menuItems.addCondition.action();

    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("makeAjaxRequest")).toHaveBeenCalledWith(
        HTTP_REQUEST_TYPE.POST, "/api/games/1/nodes/", {
            id: "createdId", parentId: "testChoiceNodeId", name: null,
            description: null, type: NODE_TYPE.CONDITION, condition:
                {flagId: "testFlagNodeId", flagState: "NOT_ACTIVE"}
        }, expect.any(Function));
});

test('initGameEditor sets add condition modal that on save button click' +
    ' after failure POST request errors should be shown', () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithFieldError);
    mockJstree_getNodeReturnsChoiceNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    menuItems.addCondition.action();

    expect(testUtils.getVar("showModalFieldErrors"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showModalFieldErrors"))
        .toHaveBeenCalledWith([{defaultMessage: "TestError"}]);
});

test('initGameEditor sets add condition modal that on save button click' +
    ' after failure POST request added node should be deleted', () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithFieldError);
    const jstreeMock =
        mockJstree_getNodeReturnsChoiceNode_createNodeReturnsCreatedId();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    menuItems.addCondition.action();

    expect(jstreeMock.delete_node).toHaveBeenCalledTimes(1);
    expect(jstreeMock.delete_node).toHaveBeenCalledWith("createdId");
});

test('initGameEditor sets add condition modal that on save button click' +
    ' after successful POST request modal should be hidden', () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithoutFieldError);

    mockJstree_getNodeReturnsChoiceNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    menuItems.addCondition.action();

    expect(testUtils.getVar("hideModal"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor sets add condition modal that on save button click' +
    ' after successful POST request jstree should be open', () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithoutFieldError);
    const jstreeMock = mockJstree_getNodeReturnsChoiceNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    menuItems.addCondition.action();

    expect(jstreeMock.open_all).toHaveBeenCalledTimes(1);
    expect(jstreeMock.open_all).toHaveBeenCalledWith(
        "testChoiceNodeId");
});

test('initGameEditor sets and shows edit room modal', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    mockJstree_getNodeReturnsRoomNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(roomNode);

    menuItems.editItem.action();

    expect(testUtils.getVar("showModal"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("prepareModalForEditingRoomNode"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("setModalSaveButtonOnClick"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor sets edit room modal that on save button click' +
    ' errors should be cleared', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    mockJstree_getNodeReturnsRoomNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(roomNode);

    menuItems.editItem.action();

    expect(testUtils.getVar("clearErrorsInModal"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor sets edit room modal that on save button click' +
    ' PUT request should be created', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("getNameValueFromModal",
        function () {
            return "newTestName";
        });
    testUtils.mockImpl("getDescValueFromModal",
        function () {
            return "newTestDesc";
        });
    mockJstree_getNodeReturnsRoomNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(roomNode);

    menuItems.editItem.action();

    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("makeAjaxRequest")).toHaveBeenCalledWith(
        HTTP_REQUEST_TYPE.PUT, "/api/games/1/nodes/testRoomNodeId", {
            name: "newTestName", description: "newTestDesc",
            type: NODE_TYPE.ROOM, condition: null
        }, expect.any(Function));
});

test('initGameEditor sets edit room modal that on save button click' +
    ' after failure PUT request errors should be shown', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithFieldError);
    mockJstree_getNodeReturnsRoomNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(roomNode);

    menuItems.editItem.action();

    expect(testUtils.getVar("showModalFieldErrors"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showModalFieldErrors"))
        .toHaveBeenCalledWith(
            [{defaultMessage: "TestError"}]);
});

test('initGameEditor sets edit room modal that on save button click' +
    ' after successful PUT request should rename node', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithoutFieldError);
    testUtils.mockImpl("getNameValueFromModal",
        function () {
            return "newTestName";
        });
    testUtils.mockImpl("getDescValueFromModal",
        function () {
            return "newTestDesc";
        });

    const jstreeMock = mockJstree_getNodeReturnsRoomNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(roomNode);

    menuItems.editItem.action();

    expect(jstreeMock.rename_node).toHaveBeenCalledTimes(1);
    expect(jstreeMock.rename_node).toHaveBeenCalledWith(
        {
            data: ["newTestDesc", null], text: "testRoomNodeName",
            parent: "testRoomNodeParent", type: NODE_TYPE.ROOM, children: [],
            id: "testRoomNodeId"
        },
        "newTestName");
});

test('initGameEditor sets edit room modal that on save button click' +
    ' after successful PUT request should change description', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithoutFieldError);
    testUtils.mockImpl("getNameValueFromModal",
        function () {
            return "newTestName";
        });
    testUtils.mockImpl("getDescValueFromModal",
        function () {
            return "newTestDesc";
        });

    const testRoomNode = {
        type: NODE_TYPE.ROOM,
        parent: 'testRoomNodeParent',
        id: "testRoomNodeId",
        text: "testRoomNodeName",
        children: [],
        data: ["testRoomNodeDescription", null]
    }

    mockJstreeImpl({
        get_node: function () {
            return testRoomNode;
        },
        create_node: jest.fn(),
        rename_node: jest.fn(),
        delete_node: jest.fn(),
        open_all: jest.fn(),
    });

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(roomNode);

    menuItems.editItem.action();

    expect(testRoomNode.data[0]).toEqual("newTestDesc");
});

test('initGameEditor sets edit room modal that on save button click' +
    ' after successful PUT request modal should be hiden', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithoutFieldError);
    mockJstree_getNodeReturnsRoomNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(roomNode);

    menuItems.editItem.action();

    expect(testUtils.getVar("hideModal"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor sets and shows edit choice modal', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    mockJstree_getNodeReturnsChoiceNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    menuItems.editItem.action();

    expect(testUtils.getVar("showModal"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("prepareModalForEditingChoiceNode"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("setModalSaveButtonOnClick"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor sets edit choice modal that on save button click' +
    ' errors should be cleared', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    mockJstree_getNodeReturnsChoiceNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    menuItems.editItem.action();

    expect(testUtils.getVar("clearErrorsInModal"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor sets edit choice modal that on save button click' +
    ' PUT request should be created when node type is CHOICE', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("getNameValueFromModal",
        function () {
            return "testName";
        });

    mockJstree_getNodeReturnsChoiceNode_createNodeReturnsCreatedId();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    menuItems.editItem.action();

    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("makeAjaxRequest")).toHaveBeenCalledWith(
        HTTP_REQUEST_TYPE.PUT, "/api/games/1/nodes/testChoiceNodeId", {
            name: "testName", description: undefined, type: NODE_TYPE.CHOICE,
            condition: null
        }, expect.any(Function));
});

test('initGameEditor sets edit choice modal that on save button click' +
    ' PUT request should be created when node type is FLAG', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("getNameValueFromModal",
        function () {
            return "testName";
        });

    mockJstree_getNodeReturnsFlagNode_createNodeReturnsCreatedId();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(flagNode);

    menuItems.editItem.action();

    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("makeAjaxRequest")).toHaveBeenCalledWith(
        HTTP_REQUEST_TYPE.PUT, "/api/games/1/nodes/testFlagNodeId", {
            name: "testName", description: undefined, type: NODE_TYPE.FLAG,
            condition: null
        }, expect.any(Function));
});

test('initGameEditor sets edit choice modal that on save button click' +
    ' after failure PUT request errors should be shown', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithFieldError);
    mockJstree_getNodeReturnsChoiceNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    menuItems.editItem.action();

    expect(testUtils.getVar("showModalFieldErrors"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showModalFieldErrors"))
        .toHaveBeenCalledWith([{defaultMessage: "TestError"}]);
});

test('initGameEditor sets edit choice modal that on save button click' +
    ' after successful PUT request node should be renamed when node type is CHOICE', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("getNameValueFromModal",
        function () {
            return "testName";
        });
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithoutFieldError);
    const jstreeMock = mockJstree_getNodeReturnsChoiceNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    menuItems.editItem.action();

    expect(jstreeMock.rename_node).toHaveBeenCalledTimes(1);
    expect(jstreeMock.rename_node).toHaveBeenCalledWith(
        {
            children: ["childId"], data: [null, null], id: "testChoiceNodeId",
            parent: "testChoiceNodeParent", text: "testChoiceNodeName",
            type: NODE_TYPE.CHOICE
        }, "testName");
});

test('initGameEditor sets edit choice modal that on save button click' +
    ' after successful PUT request node should be renamed when node type is FLAG', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("getNameValueFromModal",
        function () {
            return "testName";
        });
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithoutFieldError);
    const jstreeMock = mockJstree_getNodeReturnsFlagNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(flagNode);

    menuItems.editItem.action();

    expect(jstreeMock.rename_node).toHaveBeenCalledTimes(1);
    expect(jstreeMock.rename_node).toHaveBeenCalledWith(
        {
            children: [], data: [null, null], id: "testFlagNodeId",
            parent: "testFlagNodeParent", text: "testFlagNodeName",
            type: NODE_TYPE.FLAG
        }, "testName");
});

test('initGameEditor sets edit choice modal that on save button click' +
    ' if FLAG node is renamed, corresponding CONDITION nodes should also renamed', () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithoutFieldError);
    testUtils.mockImpl("getSelectValueFromModal", function () {
        return "testFlagNodeId";
    });
    const jstreeMock = mockJstreeImpl({
        get_node: jest.fn().mockReturnValueOnce(choiceNode)
            .mockReturnValueOnce(choiceNode)
            .mockReturnValueOnce(flagNode)
            .mockReturnValue(conditionNode),
        create_node: jest.fn(),
        delete_node: jest.fn(),
        open_all: jest.fn(),
        rename_node: jest.fn(),
    });

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItemsChoice = getJstreeContextMenuItemsFor(choiceNode);

    menuItemsChoice.addCondition.action();

    testUtils.mockImpl("createConditionName",
        function () {
            return "conditionName2";
        });

    const menuItems = getJstreeContextMenuItemsFor(flagNode);

    menuItems.editItem.action();

    expect(jstreeMock.rename_node).toHaveBeenCalledTimes(2);
    expect(jstreeMock.rename_node).toHaveBeenLastCalledWith(
        {
            children: ["childId"], data: [null, {}], id: "testConditionNodeId",
            parent: "testConditionNodeParent", text: "testConditionNodeName",
            type: NODE_TYPE.CONDITION
        }, "conditionName2");
});

test('initGameEditor sets edit choice modal that on save button click' +
    ' after successful PUT request modal should be hidden', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithoutFieldError);
    mockJstree_getNodeReturnsChoiceNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(choiceNode);

    menuItems.editItem.action();

    expect(testUtils.getVar("hideModal"))
        .toHaveBeenCalledTimes(1);
});


test('initGameEditor sets and shows edit condition modal', () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });
    testUtils.mockImpl("showModal", justRunsFunc);
    mockJstree_getNodeReturnsConditionNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(conditionNode);

    menuItems.editItem.action();

    expect(testUtils.getVar("showModal"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("prepareModalForEditingConditionNode"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("setModalSaveButtonOnClick"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor sets edit condition modal that on save button click' +
    ' errors should be cleared', () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    mockJstree_getNodeReturnsConditionNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(conditionNode);

    menuItems.editItem.action();

    expect(testUtils.getVar("clearErrorsInModal"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor sets edit condition modal that on save button click' +
    ' PUT request should be created', () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("createConditionName",
        function () {
            return "conditionName";
        });
    testUtils.mockImpl("getModalRadioValue", function () {
        return "testRadioValue";
    });
    testUtils.mockImpl("getSelectValueFromModal", function () {
        return "testFlagNodeId";
    });
    testUtils.mockImpl("getModalRadioValue", function () {
        return FLAG_STATE.FLAG_ACTIVE;
    });
    mockJstree_getNodeReturnsConditionNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(conditionNode);

    menuItems.editItem.action();

    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("makeAjaxRequest")).toHaveBeenCalledWith(
        HTTP_REQUEST_TYPE.PUT, "/api/games/1/nodes/testConditionNodeId", {
            condition: {
                flagId: "testFlagNodeId", flagState:
                FLAG_STATE.FLAG_ACTIVE
            },
            description: undefined, name: "conditionName",
            type: NODE_TYPE.CONDITION
        }, expect.any(Function));
});

test('initGameEditor sets edit condition modal that on save button click' +
    ' after failure PUT request errors should be shown', () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithFieldError);
    mockJstree_getNodeReturnsConditionNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(conditionNode);

    menuItems.editItem.action();

    expect(testUtils.getVar("showModalFieldErrors"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showModalFieldErrors"))
        .toHaveBeenCalledWith([{defaultMessage: "TestError"}]);
});

test('initGameEditor sets edit condition modal that on save button click' +
    ' after successful PUT request node should be renamed', () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithoutFieldError);
    testUtils.mockImpl("createConditionName",
        function () {
            return "conditionName";
        });
    testUtils.mockImpl("getModalRadioValue", function () {
        return "testRadioValue";
    });
    testUtils.mockImpl("getSelectValueFromModal", function () {
        return "testFlagNodeId";
    });
    testUtils.mockImpl("getModalRadioValue", function () {
        return FLAG_STATE.FLAG_ACTIVE;
    });
    const jstreeMock = mockJstree_getNodeReturnsConditionNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(conditionNode);

    menuItems.editItem.action();

    expect(jstreeMock.rename_node).toHaveBeenCalledTimes(1);
    expect(jstreeMock.rename_node).toHaveBeenCalledWith({
        children: ["childId"], data: [null,
            {flagId: "testFlagNodeId", flagState: FLAG_STATE.FLAG_ACTIVE}],
        id: "testConditionNodeId", parent: "testConditionNodeParent",
        text: "testConditionNodeName", type: NODE_TYPE.CONDITION
    }, "conditionName");
});

test('initGameEditor sets edit condition modal that on save button click' +
    ' after successful PUT request modal should be hidden', () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithoutFieldError);

    mockJstree_getNodeReturnsConditionNode();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(conditionNode);

    menuItems.editItem.action();

    expect(testUtils.getVar("hideModal"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor shows alert modal with general warning ' +
    'if trying to delete node when node type is not FLAG', () => {
    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(roomNode);

    menuItems.deleteItem.action();

    expect(testUtils.getVar("showAlertModal"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showAlertModal")).toHaveBeenCalledWith(
        DELETE_ITEM_STRING, ARE_YOU_SURE_STRING, expect.any(Function),
        ALERT_MODE.QUESTION
    );
});

test('initGameEditor shows alert modal with flag deleting warning' +
    ' if trying to delete node when node type is not FLAG', () => {
    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(flagNode);

    menuItems.deleteItem.action();

    expect(testUtils.getVar("showAlertModal"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showAlertModal")).toHaveBeenCalledWith(
        DELETE_ITEM_STRING, FLAG_DELETE_ALERT_STRING, expect.any(Function),
        ALERT_MODE.QUESTION
    );
});

test('initGameEditor sets that if trying to delete node and ' +
    ' deleting alert modal is confirmed, DELETE request should be created', () => {
    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(roomNode);

    menuItems.deleteItem.action();

    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("makeAjaxRequest")).toHaveBeenCalledWith(
        HTTP_REQUEST_TYPE.DELETE, "/api/games/1/nodes/testRoomNodeId", null,
        expect.any(Function));
});

test('initGameEditor sets that if trying to delete node and ' +
    ' deleting alert modal is confirmed, after successful DELETE request' +
    ' node should be deleted', () => {
    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);
    testUtils.mockImpl("makeAjaxRequest", testUtils.trueRequestResult);
    const jstreeMock = mockJstree();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(roomNode);

    menuItems.deleteItem.action();

    expect(jstreeMock.delete_node).toHaveBeenCalledTimes(1);
    expect(jstreeMock.delete_node).toHaveBeenCalledWith({
        children: [], data: ["testRoomNodeDescription", null],
        id: "testRoomNodeId", parent: "testRoomNodeParent",
        text: "testRoomNodeName", type: NODE_TYPE.ROOM
    });
});

test('initGameEditor sets that if trying to delete node and ' +
    ' deleting alert modal is confirmed, after successful DELETE request' +
    ' if node to delete type is FLAG, that flag should not be shown when' +
    ' trying to add CONDITION node', () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("isModalCheckboxChecked",
        function () {
            return true;
        });
    testUtils.mockImpl("getSelectValueFromModal", function () {
        return "testFlagNodeId";
    });
    mockJstree_getNodeReturnsChoiceNode_createNodeReturnsCreatedId();

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItemsRoom = getJstreeContextMenuItemsFor(otherTypeNode);

    menuItemsRoom.addChoice.action();

    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);
    testUtils.mockImpl("makeAjaxRequest", testUtils.trueRequestResult);

    const menuItems = getJstreeContextMenuItemsFor(otherTypeNode);

    menuItems.deleteItem.action();

    const menuItemsChoice = getJstreeContextMenuItemsFor(choiceNode);

    menuItemsChoice.addCondition.action();

    const prepareModalForAddingConditionNodeCall =
        prepareModalForAddingConditionNode.mock.calls[0][0];

    expect(prepareModalForAddingConditionNodeCall.length).toBe(1);
    expect(prepareModalForAddingConditionNodeCall[0]).toEqual(placeholderFlag);
});

test('initGameEditor sets that if trying to delete node and ' +
    ' deleting alert modal is confirmed, after successful DELETE request' +
    ' if node to delete type is FLAG, should delete also corresponding' +
    ' CONDITION nodes', () => {
    testUtils.mockImpl("getAllFlags", function () {
        return [placeholderFlag];
    });
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);
    testUtils.mockImpl("makeAjaxRequest", testUtils.trueRequestResult);
    testUtils.mockImpl("getSelectValueFromModal", function () {
        return "testFlagNodeId";
    });
    testUtils.mockImpl("getModalRadioValue", function () {
        return FLAG_STATE.FLAG_ACTIVE;
    });
    testUtils.mockImpl("isModalCheckboxChecked",
        function () {
            return true;
        });
    const jstreeMock = mockJstreeImpl({
        get_node: jest.fn().mockReturnValueOnce(choiceNode)
            .mockReturnValueOnce(choiceNode)
            .mockReturnValueOnce(flagNode)
            .mockReturnValueOnce(conditionNode)
            .mockReturnValueOnce(flagNode),
        create_node: jest.fn().mockReturnValue("createdId"),
        delete_node: jest.fn(),
        open_all: jest.fn(),
        rename_node: jest.fn(),
    });

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItemsChoice = getJstreeContextMenuItemsFor(choiceNode);

    menuItemsChoice.addCondition.action();

    const menuItemsFlag = getJstreeContextMenuItemsFor(flagNode);

    menuItemsFlag.deleteItem.action();

    expect(jstreeMock.delete_node).toHaveBeenCalledTimes(2);
    expect(jstreeMock.delete_node).toHaveBeenLastCalledWith("createdId");

    menuItemsFlag.editItem.action();

    expect(jstreeMock.rename_node).toHaveBeenCalledTimes(1);
});

test('initGameEditor sets that if trying to delete node and ' +
    ' deleting alert modal is confirmed, after failed DELETE request' +
    ' alert should be shown', () => {
    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);
    testUtils.mockImpl("makeAjaxRequest", testUtils.falseRequestResult);

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const menuItems = getJstreeContextMenuItemsFor(roomNode);

    menuItems.deleteItem.action();

    expect(testUtils.getVar("showAlertModal"))
        .toHaveBeenCalledTimes(2);
    expect(testUtils.getVar("showAlertModal")).toHaveBeenLastCalledWith(
        DELETE_ITEM_STRING, CAN_NOT_DELETE_ITEM_STRING, null, ALERT_MODE.INFO);
});

test('initGameEditor sets that if root node is not null play button' +
    ' should be shown', () => {
    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    expect(testUtils.getVar("showPlayButton"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor sets that if root node is not null edit button' +
    ' should be shown', () => {
    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    expect(testUtils.getVar("showEditInfoButton"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor sets that if root node is null play button' +
    ' should be hidden', () => {
    gameEditorModule.initGameEditor(testGameToEditWithNullRootNode,
        testIconsUrl);

    expect(testUtils.getVar("hidePlayButton"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor sets that if root node is null edit button' +
    ' should be hidden', () => {
    gameEditorModule.initGameEditor(testGameToEditWithNullRootNode,
        testIconsUrl);

    expect(testUtils.getVar("hideEditInfoButton"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor sets that if root node is null modal closing' +
    ' should be prevented', () => {
    gameEditorModule.initGameEditor(testGameToEditWithNullRootNode,
        testIconsUrl);

    expect(testUtils.getVar("preventClosingModal"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor sets that if root node is null, main room adding' +
    ' modal should be set and shown', () => {
    testUtils.mockImpl("showModal", justRunsFunc);

    gameEditorModule.initGameEditor(testGameToEditWithNullRootNode,
        testIconsUrl);

    expect(testUtils.getVar("showModal"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("prepareModalForAddingRoomNode"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("setModalSaveButtonOnClick"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor sets that if root node is null, ' +
    ' add main room modal is showed, and if save button is clicked, ' +
    ' errors in modal should be cleared', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);

    gameEditorModule.initGameEditor(testGameToEditWithNullRootNode,
        testIconsUrl);

    expect(testUtils.getVar("clearErrorsInModal"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor sets that if root node is null, ' +
    ' add main room modal is showed, and if save button is clicked,' +
    ' play button should be shown', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);

    gameEditorModule.initGameEditor(testGameToEditWithNullRootNode,
        testIconsUrl);

    expect(testUtils.getVar("showPlayButton"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor sets that if root node is null, ' +
    ' add main room modal is showed, and if save button is clicked,' +
    ' edit info button should be shown', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);

    gameEditorModule.initGameEditor(testGameToEditWithNullRootNode,
        testIconsUrl);

    expect(testUtils.getVar("showEditInfoButton"))
        .toHaveBeenCalledTimes(1);
});

test('initGameEditor sets that if root node is null, ' +
    ' add main room modal is showed, and if save button is clicked,' +
    ' main room node should be created', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("getNameValueFromModal", function () {
        return "testName";
    });
    testUtils.mockImpl("getDescValueFromModal", function () {
        return "testDesc";
    });
    const jstreeMock = mockJstree();

    gameEditorModule.initGameEditor(testGameToEditWithNullRootNode,
        testIconsUrl);

    expect(jstreeMock.create_node).toHaveBeenCalledTimes(1);
    expect(jstreeMock.create_node).toHaveBeenCalledWith(null,
        {data: ["testDesc"], text: "testName", type: NODE_TYPE.ROOM});
});

test('initGameEditor sets that if root node is null, ' +
    ' add main room modal is showed, and if save button is clicked,' +
    ' POST request should be created', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("getNameValueFromModal", function () {
        return "testName";
    });
    testUtils.mockImpl("getDescValueFromModal", function () {
        return "testDesc";
    });
    mockJstree_createNodeReturnsCreatedId();

    gameEditorModule.initGameEditor(testGameToEditWithNullRootNode,
        testIconsUrl);

    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("makeAjaxRequest")).toHaveBeenCalledWith(
        HTTP_REQUEST_TYPE.POST, "/api/games/1/nodes/",
        {
            condition: undefined, description: "testDesc", id: "createdId",
            name: "testName", parentId: "###", type: NODE_TYPE.ROOM
        },
        expect.any(Function));
});

test('initGameEditor sets that if root node is null, ' +
    ' add main room modal is showed, and if save button is clicked' +
    ' and POST request is failed, errors should be shown', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithFieldError);
    mockJstree_createNodeReturnsCreatedId();

    gameEditorModule.initGameEditor(testGameToEditWithNullRootNode,
        testIconsUrl);

    expect(testUtils.getVar("showModalFieldErrors"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showModalFieldErrors"))
        .toHaveBeenCalledWith([{defaultMessage: "TestError"}]);
});

test('initGameEditor sets that if root node is null, ' +
    ' add main room modal is showed, and if save button is clicked' +
    ' and POST request is failed, added node should be deleted', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithFieldError);
    const jstreeMock = mockJstree_createNodeReturnsCreatedId();

    gameEditorModule.initGameEditor(testGameToEditWithNullRootNode,
        testIconsUrl);

    expect(jstreeMock.delete_node).toHaveBeenCalledTimes(1);
    expect(jstreeMock.delete_node).toHaveBeenCalledWith("createdId");
});

test('initGameEditor sets that if root node is null, ' +
    ' add main room modal is showed, and if save button is clicked' +
    ' and POST request is succeed, modal should be hidden', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithoutFieldError);
    mockJstree_createNodeReturnsCreatedId();

    gameEditorModule.initGameEditor(testGameToEditWithNullRootNode,
        testIconsUrl);

    expect(testUtils.getVar("hideModal"))
        .toHaveBeenCalledTimes(1);
});

test('showCreateGameModal prevents modal from closing', () => {
    gameEditorModule.showCreateGameModal();

    expect(testUtils.getVar("preventClosingModal"))
        .toHaveBeenCalledTimes(1);
});

test('showCreateGameModal sets and shows create game modal', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    gameEditorModule.showCreateGameModal();

    expect(testUtils.getVar("showModal")).toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("prepareModalForCreatingGame"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("setModalSaveButtonOnClick"))
        .toHaveBeenCalledTimes(1);
});

test('showCreateGameModal sets create game modal that if save button ' +
    ' is clicked, errors in modal should be cleared', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);

    gameEditorModule.showCreateGameModal();

    expect(testUtils.getVar("clearErrorsInModal"))
        .toHaveBeenCalledTimes(1);
});

test('showCreateGameModal sets create game modal that if save button' +
    ' is clicked POST request should be created', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("getNameValueFromModal", function () {
        return "testName";
    });
    testUtils.mockImpl("getDescValueFromModal", function () {
        return "testDesc";
    });
    testUtils.mockImpl("getSelectValueFromModal", function () {
        return "testLang";
    });

    gameEditorModule.showCreateGameModal();

    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledWith(HTTP_REQUEST_TYPE.POST, "/api/games/",
            {name: "testName", description: "testDesc", language: "testLang"},
            expect.any(Function));
});

test('showCreateGameModal sets create game modal that if save button ' +
    ' is clicked, after failure POST request errors should be shown', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithFieldError);

    gameEditorModule.showCreateGameModal();

    expect(testUtils.getVar("showModalFieldErrors"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showModalFieldErrors"))
        .toHaveBeenCalledWith([{defaultMessage: "TestError"}]);
});

test('showCreateGameModal sets create game modal that if save button ' +
    'is clicked, after success POST request should be redirect to edit page', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest", function (type, url, data, func) {
        data = {
            responseJSON: 1
        }
        func(data);
    });
    const locationMock = testUtils.mockLocation();

    gameEditorModule.showCreateGameModal();

    expect(locationMock.href).toEqual("/games/1/edit");
});

test('showUpdateGameInfoModal sets and shows update game info modal', () => {
    testUtils.mockImpl("showModal", justRunsFunc);

    gameEditorModule.initGameEditor(testGameToEditWithNotNullRootNode,
        testIconsUrl);

    const game = testGameToEditWithNotNullRootNode;

    gameEditorModule.showUpdateGameInfoModal(game.id, game.name,
        game.description, game.language);

    expect(testUtils.getVar("showModal"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("prepareModalForUpdatingGameInfo"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("prepareModalForUpdatingGameInfo"))
        .toHaveBeenCalledWith("testGameName", "testGameDescription",
            "testGameLanguage");
    expect(testUtils.getVar("setModalSaveButtonOnClick"))
        .toHaveBeenCalledTimes(1);
});

test('showUpdateGameInfoModal sets update game modal that ' +
    ' on save button click errors should be cleared', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);

    gameEditorModule.initGameEditor(testGameToEditWithNullRootNode,
        testIconsUrl);

    const game = testGameToEditWithNotNullRootNode;

    gameEditorModule.showUpdateGameInfoModal(game.id, game.name,
        game.description, game.language);

    expect(testUtils.getVar("clearErrorsInModal"))
        .toHaveBeenCalledTimes(2);
});

test('showUpdateGameInfoModal sets update game modal that ' +
    ' on save button click POST request should be created', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("getNameValueFromModal", function () {
        return "testName";
    });
    testUtils.mockImpl("getDescValueFromModal", function () {
        return "testDesc";
    });
    testUtils.mockImpl("getSelectValueFromModal", function () {
        return "testLang";
    });
    testUtils.mockImpl("isModalCheckboxChecked", function () {
        return true;
    });

    gameEditorModule.initGameEditor(testGameToEditWithNullRootNode,
        testIconsUrl);

    const game = testGameToEditWithNotNullRootNode;

    gameEditorModule.showUpdateGameInfoModal(game.id, game.name,
        game.description, game.language);

    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledTimes(2);
    expect(testUtils.getVar("makeAjaxRequest")).toHaveBeenLastCalledWith(
        HTTP_REQUEST_TYPE.PUT, "/api/games/1", {
            description: "testDesc", language: "testLang",
            name: "testName", published: true
        }, expect.any(Function));
});

test('showUpdateGameInfoModal sets update game modal that ' +
    ' on save button click after failure POST request errors should be shown', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithFieldError);

    gameEditorModule.initGameEditor(testGameToEditWithNullRootNode,
        testIconsUrl);

    const game = testGameToEditWithNotNullRootNode;

    gameEditorModule.showUpdateGameInfoModal(game.id, game.name,
        game.description, game.language);

    expect(testUtils.getVar("showModalFieldErrors"))
        .toHaveBeenCalledTimes(2);
    expect(testUtils.getVar("showModalFieldErrors"))
        .toHaveBeenCalledWith([{defaultMessage: "TestError"}]);
});

test('showUpdateGameInfoModal sets update game modal that ' +
    ' on save button click after success POST request modal should be hidden', () => {
    testUtils.mockImpl("showModal", justRunsFunc);
    testUtils.mockImpl("setModalSaveButtonOnClick", justRunsFunc);
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithoutFieldError);

    gameEditorModule.initGameEditor(testGameToEditWithNullRootNode,
        testIconsUrl);

    const game = testGameToEditWithNotNullRootNode;

    gameEditorModule.showUpdateGameInfoModal(game.id, game.name,
        game.description, game.language);

    expect(testUtils.getVar("hideModal"))
        .toHaveBeenCalledTimes(2);
});

function mockJstree() {
    $.fn.jstree = jest.fn();

    const jstreeMock = {
        get_node: jest.fn(),
        create_node: jest.fn(),
        delete_node: jest.fn(),
        open_all: jest.fn(),
    }

    jest.spyOn($.fn, 'jstree').mockReturnValue({
        jstree: jest.fn().mockImplementation(function () {
            return jstreeMock;
        }),
        bind: jest.fn().mockImplementation(function (event, func) {
            if (event === "loaded.jstree") {
                func();
            }

            jstreeMock.jstree = jest.fn().mockImplementation(function () {
                return jstreeMock;
            });

            return jstreeMock;
        })
    });

    return jstreeMock;
}

function mockJstreeImpl(impl) {
    jest.spyOn($.fn, 'jstree').mockReturnValue({
        jstree: jest.fn().mockImplementation(function () {
            return impl;
        }),
        bind: jest.fn().mockImplementation(function (event, func) {
            if (event === "loaded.jstree") {
                func();
            }

            impl.jstree = jest.fn().mockImplementation(function () {
                return impl;
            });

            return impl;
        })
    });
    return impl;
}

function mockJstree_getNodeReturnsRoomNode() {
    const jstreeMock = {
        get_node: function () {
            return roomNode;
        },
        create_node: jest.fn(),
        rename_node: jest.fn(),
        delete_node: jest.fn(),
        open_all: jest.fn(),
    }
    return mockJstreeImpl(jstreeMock);
}

function mockJstree_getNodeReturnsChoiceNode() {
    const jstreeMock = {
        get_node: function () {
            return choiceNode;
        },
        create_node: jest.fn(),
        delete_node: jest.fn(),
        open_all: jest.fn(),
        rename_node: jest.fn(),
    }
    return mockJstreeImpl(jstreeMock);
}

function mockJstree_getNodeReturnsFlagNode() {
    const jstreeMock = {
        get_node: function () {
            return flagNode;
        },
        create_node: jest.fn(),
        delete_node: jest.fn(),
        open_all: jest.fn(),
        rename_node: jest.fn(),
    }
    return mockJstreeImpl(jstreeMock);
}

function mockJstree_getNodeReturnsConditionNode() {
    const jstreeMock = {
        get_node: function () {
            return conditionNode;
        },
        create_node: jest.fn(),
        delete_node: jest.fn(),
        open_all: jest.fn(),
        rename_node: jest.fn(),
    }
    return mockJstreeImpl(jstreeMock);
}

function mockJstree_getNodeReturnsChoiceNode_createNodeReturnsCreatedId() {
    const jstreeMock = {
        get_node: function () {
            return choiceNode;
        },
        create_node: function () {
            return "createdId";
        },
        delete_node: jest.fn(),
        open_all: jest.fn(),
        rename_node: jest.fn(),
    }
    return mockJstreeImpl(jstreeMock);
}

function mockJstree_getNodeReturnsFlagNode_createNodeReturnsCreatedId() {
    const jstreeMock = {
        get_node: function () {
            return flagNode;
        },
        create_node: function () {
            return "createdId";
        },
        delete_node: jest.fn(),
        open_all: jest.fn(),
        rename_node: jest.fn(),
    }
    return mockJstreeImpl(jstreeMock);
}

function mockJstree_createNodeReturnsCreatedId() {
    const jstreeMock = {
        get_node: jest.fn(),
        create_node: function () {
            return "createdId";
        },
        delete_node: jest.fn(),
        open_all: jest.fn(),
        rename_node: jest.fn(),
    }
    return mockJstreeImpl(jstreeMock);
}

function getJstreeContextMenuItemsFor(node) {
    const jstreeMockCall = $().jstree.mock.calls[0][0];
    return jstreeMockCall.contextmenu.items(node);
}

function justRunsFunc(func) {
    func();
}