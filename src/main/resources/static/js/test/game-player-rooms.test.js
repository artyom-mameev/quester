/**
 * @jest-environment jsdom
 */

const gamePlayer_RoomsModule = require("../game-player-rooms");
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
    testUtils.mockFuncs("getNextReachableRoom");

    testUtils.mockImpl("getNextReachableRoom", function () {
        return undefined;
    });
}

test('getNextChoices tries to get next reachable room if node type is CHOICE', () => {
    gamePlayer_RoomsModule.getNextChoices(
        roomWithChildren(1, [
            choiceWithChildren(2, [])
        ]), []);

    expect(testUtils.getVar("getNextReachableRoom"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("getNextReachableRoom"))
        .toHaveBeenCalledWith(choiceWithChildren(2, []), []);
});

test('getNextChoices returns empty choices if room has no reachable rooms', () => {
    const choices = gamePlayer_RoomsModule.getNextChoices(
        roomWithChildren(1, []), []);

    expect(choices.length).toBe(0);
});

test('getNextChoices returns proper choices if room has reachable rooms', () => {
    testUtils.getVar("getNextReachableRoom")
        .mockImplementationOnce(function () {
            return undefined;
        }).mockImplementationOnce(function () {
        return roomWithChildren(4, []);
    });

    const choices = gamePlayer_RoomsModule.getNextChoices(
        roomWithChildren(1, [
            choiceWithChildren(1, [
                roomWithChildren(2, [])
            ]),
            choiceWithChildren(2, [
                roomWithChildren(3, [])
            ])
        ]), []);

    expect(choices.length).toBe(1);
    expect(choices[0].id).toEqual("testChoiceId2");
    expect(choices[0].text).toEqual("testChoiceName2");
    expect(choices[0].type).toEqual(NODE_TYPE.CHOICE);
    expect(choices[0].nextRoom).toEqual(roomWithChildren(4, []));
});

test('getNextChoices returns proper choices if room has flags', () => {
    const room = roomWithChildren(1, [
        flagWithChildren(1, []),
        flagWithChildren(2, [])
    ]);
    const choices = gamePlayer_RoomsModule.getNextChoices(room,
        ["testFlagId1"]);

    expect(choices.length).toBe(1);
    expect(choices[0].id).toEqual("testFlagId2");
    expect(choices[0].text).toEqual("testFlagName2");
    expect(choices[0].type).toEqual(NODE_TYPE.FLAG);
    expect(choices[0].nextRoom).toEqual(room);
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

function choiceWithChildren(id, children) {
    return {
        id: "testChoiceId" + id,
        type: NODE_TYPE.CHOICE,
        name: "testChoiceName" + id,
        children: children
    }
}

function flagWithChildren(id, children) {
    return {
        id: "testFlagId" + id,
        type: NODE_TYPE.FLAG,
        name: "testFlagName" + id,
        children: children
    }
}