/**
 * @jest-environment jsdom
 */

const gameOperations_ApiModule = require("../game-operations-api");
const testUtils = require("./util/test-utils.js")

beforeAll(() => {
    setUpJquery();
    setUpGlobalVariables();
    setUpMocks();
});

function setUpJquery() {
    global.$ = require("jquery");
    global.jQuery = global.$;
}

function setUpGlobalVariables() {
    global.CANNOT_MAKE_A_FAVORITE_STRING = "CANNOT_MAKE_A_FAVORITE_STRING";
    global.CANNOT_UNFAVORITE_STRING = "CANNOT_UNFAVORITE_STRING";
    global.DELETE_GAME_STRING = "DELETE_GAME_STRING";
    global.CANNOT_DELETE_THE_GAME_STRING = "CANNOT_DELETE_THE_GAME_STRING";
    global.ARE_YOU_SURE_STRING = "ARE_YOU_SURE_STRING";

    global.ALERT_MODE = {
        INFO: "INFO",
        QUESTION: "QUESTION"
    };
    global.HTTP_REQUEST_TYPE = {
        POST: "POST",
        PUT: "PUT",
        DELETE: "DELETE",
    }
}

function setUpMocks() {
    $.fn.rating = jest.fn();

    testUtils.mockFuncs("makeAjaxRequest", "showSimpleErrorModal",
        "showAlertModal", "changeToFavoriteState", "changeToUnfavoriteState");
}

test('favorite makes POST request', () => {
    gameOperations_ApiModule.favorite(1);

    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("makeAjaxRequest")).toHaveBeenCalledWith(
        HTTP_REQUEST_TYPE.POST, "/api/games/1/favorites", null,
        expect.any(Function));
});

test('favorite sets that after successful POST request favorite button' +
    ' should be changed to favorited state', () => {
    testUtils.mockImpl("makeAjaxRequest", testUtils.trueRequestResult);

    gameOperations_ApiModule.favorite(1);

    expect(testUtils.getVar("changeToFavoriteState"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("changeToFavoriteState"))
        .toHaveBeenCalledWith(1);
});

test('favorite sets that after failure POST request error should be shown', () => {
    testUtils.mockImpl("makeAjaxRequest", testUtils.falseRequestResult);

    gameOperations_ApiModule.favorite(1);

    expect(testUtils.getVar("showSimpleErrorModal"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showSimpleErrorModal")).toHaveBeenCalledWith(
        CANNOT_MAKE_A_FAVORITE_STRING);
});

test('unfavorite makes POST request', () => {
    gameOperations_ApiModule.unfavorite(1);

    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("makeAjaxRequest")).toHaveBeenCalledWith(
        HTTP_REQUEST_TYPE.DELETE, "/api/games/1/favorites", null,
        expect.any(Function));
});

test('unfavorite sets that after successful POST request favorite button' +
    ' should be changed to unfavorited state', () => {
    testUtils.mockImpl("makeAjaxRequest", testUtils.trueRequestResult);

    gameOperations_ApiModule.unfavorite(1);

    expect(testUtils.getVar("changeToUnfavoriteState"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("changeToUnfavoriteState"))
        .toHaveBeenCalledWith(1);
});

test('unfavorite sets that after failure POST request error should be shown', () => {
    testUtils.mockImpl("makeAjaxRequest", testUtils.falseRequestResult);

    gameOperations_ApiModule.unfavorite(1);

    expect(testUtils.getVar("showSimpleErrorModal"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showSimpleErrorModal")).toHaveBeenCalledWith(
        CANNOT_UNFAVORITE_STRING);
});

test('deleteGame shows alert modal', () => {
    gameOperations_ApiModule.deleteGame(1);

    expect(testUtils.getVar("showAlertModal"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showAlertModal")).toHaveBeenCalledWith(
        DELETE_GAME_STRING, ARE_YOU_SURE_STRING, expect.any(Function),
        ALERT_MODE.QUESTION);
});

test('deleteGame sets that after confirming alert modal, POST request' +
    ' should be created', () => {
    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);

    gameOperations_ApiModule.deleteGame(1);

    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("makeAjaxRequest")).toHaveBeenCalledWith(
        HTTP_REQUEST_TYPE.DELETE, "/api/games/1", null, expect.any(Function));
});

test('deleteGame sets that after confirming alert modal and successful' +
    ' POST request page should redirect to games url', () => {
    testUtils.mockImpl("makeAjaxRequest", testUtils.trueRequestResult);
    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);
    const locationMock = testUtils.mockLocation();

    gameOperations_ApiModule.deleteGame("testId");

    expect(locationMock.href).toEqual("testOrigin" + "/games");
});

test('deleteGame sets that after confirming alert modal and failure' +
    ' POST request error should be shown', () => {
    testUtils.mockImpl("makeAjaxRequest", testUtils.falseRequestResult);
    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);

    gameOperations_ApiModule.deleteGame(1);

    expect(testUtils.getVar("showSimpleErrorModal"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showSimpleErrorModal")).toHaveBeenCalledWith(
        CANNOT_DELETE_THE_GAME_STRING);
});