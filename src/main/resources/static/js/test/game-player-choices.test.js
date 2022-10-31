/**
 * @jest-environment jsdom
 */

const gamePlayer_ChoicesModule = require("../game-player-choices");

beforeAll(() => {
    setUpGlobalVariables();
});

function setUpGlobalVariables() {
    global.NODE_TYPE = {
        ROOM: "ROOM",
        CHOICE: "CHOICE",
        FLAG: "FLAG",
        CONDITION: "CONDITION"
    }

    global.FLAG_STATE = {
        FLAG_ACTIVE: "modal-radio-condition-active",
        FLAG_NOT_ACTIVE: "modal-radio-condition-not-active"
    }
}

test('getNextReachableRoom returns room if children has room', () => {
    expect(gamePlayer_ChoicesModule.getNextReachableRoom(
        choiceWithChildren(1, [
            roomWithChildren(1, [])
        ]), [])).toEqual(roomWithChildren(1, []));
});

test('getNextReachableRoom returns room from triggered active condition ' +
    ' even if room has normal room', () => {
    expect(gamePlayer_ChoicesModule.getNextReachableRoom(
        choiceWithChildren(1, [
            roomWithChildren(1, []),
            conditionWithChildren(1, "flagId", FLAG_STATE.FLAG_ACTIVE, [
                conditionWithChildren(1, "flagId2", FLAG_STATE.FLAG_ACTIVE, [
                    roomWithChildren(2, [])
                ])
            ])
        ]), ["flagId", "flagId2"])).toEqual(roomWithChildren(2, []));
});

test('getNextReachableRoom returns room from triggered not active condition ' +
    ' even if room has normal room', () => {
    expect(gamePlayer_ChoicesModule.getNextReachableRoom(
        choiceWithChildren(1, [
            roomWithChildren(1, []),
            conditionWithChildren(1, "flagId", FLAG_STATE.FLAG_NOT_ACTIVE, [
                conditionWithChildren(1, "flagId2", FLAG_STATE.FLAG_NOT_ACTIVE, [
                    roomWithChildren(2, [])
                ])
            ])
        ]), [])).toEqual(roomWithChildren(2, []));
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

function conditionWithChildren(id, flagId, flagState, children) {
    return {
        id: "testConditionId" + id,
        type: NODE_TYPE.CONDITION,
        name: "testConditionName" + id,
        condition: {
            flagId: flagId,
            flagState: flagState
        },
        children: children
    }
}