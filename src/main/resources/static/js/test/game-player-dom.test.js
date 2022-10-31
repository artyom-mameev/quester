/**
 * @jest-environment jsdom
 */

const gamePlayer_DomModule = require("../game-player-dom");
const testUtils = require("./util/test-utils.js")

beforeAll(() => {
    setUpJquery();
    setUpGlobalVariables();
    setUpMocks();
});

beforeEach(() => {
    setUpHtml();
});

function setUpJquery() {
    global.$ = require("jquery");
    global.jQuery = global.$;
}

function setUpGlobalVariables() {
    global.START_AGAIN_STRING = "START_AGAIN_STRING";
    global.RETURN_TO_GAME_PAGE_STRING = "RETURN_TO_GAME_PAGE_STRING";

    global.NODE_TYPE = {
        ROOM: "ROOM",
        CHOICE: "CHOICE",
        FLAG: "FLAG",
        CONDITION: "CONDITION"
    }
}

function setUpMocks() {
    testUtils.mockFuncs("goToNextRoom");
}

function setUpHtml() {
    document.body.innerHTML =
        "                <div id=\"game\" data-game-id=\"1\">\n" +
        "                    <h5 id=\"room-name\"></h5>\n" +
        "                    <p><label id=\"room-description\"></label></p>\n" +
        "                    <div id=\"choices\">\n" +
        "                    </div>\n" +
        "                </div>\n";
}

test('changeRoomTitle changes room title', () => {
    gamePlayer_DomModule.changeRoomTitle("roomTitle");

    expect($("#room-name").html()).toEqual("roomTitle");
});

test('changeRoomDesc changes room description', () => {
    gamePlayer_DomModule.changeRoomDesc("roomDesc");

    expect($("#room-description").html()).toEqual("roomDesc");
});

test('createGameChoice adds choices to page', () => {
    gamePlayer_DomModule.createGameChoice({
        text: "testText", id: "testId", type: NODE_TYPE.CHOICE,
        nextRoom: roomWithChildren(1, [])
    }, []);

    gamePlayer_DomModule.createGameChoice({
        text: "testText2", id: "testId2", type: NODE_TYPE.FLAG,
        nextRoom: roomWithChildren(2, [])
    }, []);

    expect(hasChoices([
        {
            id: "testId",
            text: "testText"
        },
        {
            id: "testId2",
            text: "testText2"
        }
    ])).toBe(true);
});

test('createGameChoice sets up added choices that on click' +
    ' goToNextRoom should be called', () => {
    gamePlayer_DomModule.createGameChoice({
        text: "testText", id: "testId", type: NODE_TYPE.CHOICE,
        nextRoom: roomWithChildren(1, [])
    }, []);

    gamePlayer_DomModule.createGameChoice({
        text: "testText2", id: "testId2", type: NODE_TYPE.FLAG,
        nextRoom: roomWithChildren(2, [])
    }, []);

    clickOnChoice("testId");

    expect(testUtils.getVar("goToNextRoom"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("goToNextRoom")).toHaveBeenCalledWith(
        roomWithChildren(1, []));

    clickOnChoice("testId2");

    expect(testUtils.getVar("goToNextRoom"))
        .toHaveBeenCalledTimes(2);
    expect(testUtils.getVar("goToNextRoom")).toHaveBeenNthCalledWith(
        2, roomWithChildren(2, []));
});

test('createGameChoice should add FLAG nodes to flags array', () => {
    const flags = [];

    gamePlayer_DomModule.createGameChoice({
        text: "testText", id: "testId", type: NODE_TYPE.FLAG,
        nextRoom: roomWithChildren(1, [])
    }, flags);

    clickOnChoice("testId")

    expect(flags.length).toBe(1);
    expect(flags[0]).toEqual("testId");
});

test('createGameOverChoices should add game over choices to page', () => {
    gamePlayer_DomModule.createGameOverChoices();

    expect(hasChoices([
        {
            id: "start-again",
            text: START_AGAIN_STRING
        },
        {
            id: "return",
            text: RETURN_TO_GAME_PAGE_STRING
        }
    ])).toBe(true);
});

test('createGameOverChoices sets up added choices that ' +
    ' on start again choice click page should be reloaded', () => {
    const locationMock = testUtils.mockLocation();

    gamePlayer_DomModule.createGameOverChoices();

    clickOnChoice("start-again");

    expect(locationMock.reload).toHaveBeenCalledTimes(1);
});

test('createGameOverChoices sets up added choices that ' +
    ' on return choice click page should be redirected to game page', () => {
    const locationMock = testUtils.mockLocation();

    gamePlayer_DomModule.createGameOverChoices();

    clickOnChoice("return");

    expect(locationMock.assign).toHaveBeenCalledTimes(1);
    expect(locationMock.assign).toHaveBeenCalledWith("testOrigin/games/1");
});

test('clearChoices should clear all choices', () => {
    const choices = $('#choices');

    choices.append('<a href="#" id="1">Choice</a><br id=1br>');

    gamePlayer_DomModule.clearChoices();

    expect(choices.is(':empty')).toBe(true);
});

function hasChoices(choices) {
    for (const choice of choices) {
        const htmlChoice = $('#choices > a#' + choice.id);
        if ((htmlChoice.text() !== choice.text)) {
            return false;
        }
    }
    return $('#choices a').length === choices.length;

}

function roomWithChildren(id, children) {
    return {
        id: "testRoomId" + id,
        type: NODE_TYPE.ROOM,
        name: "testRoomName" + id,
        description: "testRoomDesc" + id,
        children: children
    }
}

function clickOnChoice(id) {
    $('#' + id).trigger("click");
}