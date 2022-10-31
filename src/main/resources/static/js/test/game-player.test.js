/**
 * @jest-environment jsdom
 */
const gamePlayerModule = require("../game-player");
const testUtils = require("./util/test-utils.js")

beforeAll(() => {
    setUpGlobalVariables();
    setUpMocks();
});

function setUpGlobalVariables() {
    global.NODE_TYPE = {
        ROOM: "ROOM",
        CHOICE: "CHOICE",
        FLAG: "FLAG",
        CONDITION: "CONDITION"
    }
}

function setUpMocks() {
    testUtils.mockFuncs("changeRoomTitle", "changeRoomDesc",
        "clearChoices", "createGameChoice", "createGameOverChoices");
    testUtils.mockImpl("getNextChoices", function () {
        return [];
    });
}

test('goToNextRoom clears choices', () => {
    gamePlayerModule.goToNextRoom(roomWithChildren(1, []));

    expect(testUtils.getVar("clearChoices"))
        .toHaveBeenCalledTimes(1);
});

test('goToNextRoom changes room info', () => {
    gamePlayerModule.goToNextRoom(roomWithChildren(1, []));

    expect(testUtils.getVar("changeRoomTitle"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("changeRoomTitle"))
        .toHaveBeenCalledWith("testRoomName1");
    expect(testUtils.getVar("changeRoomDesc"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("changeRoomDesc"))
        .toHaveBeenCalledWith("testRoomDesc1");
});

test('goToNextRoom creates game over choices if room has no choices ', () => {
    gamePlayerModule.goToNextRoom(roomWithChildren(1, []));

    expect(testUtils.getVar("createGameOverChoices"))
        .toHaveBeenCalledTimes(1);
});

test('goToNextRoom creates choices if room has choices', () => {
    const testChoice = {
        id: "testChoiceId", text: "testChoiceText", type: NODE_TYPE.CHOICE
    }
    const testFlag = {
        id: "testFlagId", text: "testFlagText", type: NODE_TYPE.FLAG
    }

    testUtils.mockImpl("getNextChoices", function () {
        return [testChoice, testFlag];
    });

    gamePlayerModule.goToNextRoom(roomWithChildren(1, []));

    expect(testUtils.getVar("createGameChoice"))
        .toHaveBeenCalledTimes(2);
    expect(testUtils.getVar("createGameChoice")).toHaveBeenNthCalledWith(
        1, testChoice, expect.any(Array));
    expect(testUtils.getVar("createGameChoice")).toHaveBeenNthCalledWith(
        2, testFlag, expect.any(Array));
});

function roomWithChildren(id, children) {
    return {
        id: "testRoomId" + id,
        type: NODE_TYPE.ROOM,
        name: "testRoomName" + id,
        description: "testRoomDesc" + id,
        children: children
    }
}