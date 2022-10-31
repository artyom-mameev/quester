/**
 * @jest-environment jsdom
 */

const userModalModule = require("../user-modal");
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
    global.BAN_USER_STRING = "BAN_USER_STRING";
    global.ARE_YOU_SURE_STRING = "ARE_YOU_SURE_STRING";
    global.CAN_NOT_BAN_STRING = "CAN_NOT_BAN_STRING";
    global.UNBAN_USER_STRING = "UNBAN_USER_STRING";
    global.CAN_NOT_UNBAN_STRING = "CAN_NOT_UNBAN_STRING";
    global.DELETE_ALL_GAMES_STRING = "DELETE_ALL_GAMES_STRING";
    global.CAN_NOT_DELETE_ALL_GAMES_STRING = "CAN_NOT_DELETE_ALL_GAMES_STRING";
    global.DELETE_ALL_COMMENTS_STRING = "DELETE_ALL_COMMENTS_STRING";
    global.CAN_NOT_DELETE_ALL_COMMENTS_STRING = "CAN_NOT_DELETE_ALL_COMMENTS_STRING";
    global.DELETE_ALL_RATED_STRING = "DELETE_ALL_RATED_STRING";
    global.CAN_NOT_DELETE_ALL_RATED_STRING = "CAN_NOT_DELETE_ALL_RATED_STRING";

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
    jest.spyOn($.fn, 'off');
    jest.spyOn($.fn, 'on');
    jest.spyOn($.fn, 'click');
    jest.spyOn($.fn, 'text');
    jest.spyOn($.fn, 'attr');
    jest.spyOn($.fn, 'removeAttr');

    $.fn.modal = jest.fn();

    testUtils.mockFuncs("showAlertModal", "makeAjaxRequest",
        "showSimpleErrorModal");
}

function setUpHtml() {
    document.body.innerHTML = "<a id=\"enabled-user-zero-count-link\" data-user-name=\"enabled-user-zero-count-name\" data-user-id=\"1\" data-user-enabled=\"true\" data-user-rating=\"1.0\" data-user-games-count=\"0\" data-user-rated-count=\"0\" data-user-comments-count=\"0\"\n" +
        "                           data-toggle=\"modal\" data-target=\"#userModal\" href=\"#\"></a>" +
        "<a id=\"disabled-user-one-count-link\" data-user-name=\"disabled-user-one-count-name\" data-user-id=\"2\" data-user-enabled=\"false\" data-user-rating=\"1.0\" data-user-games-count=\"1\" data-user-rated-count=\"1\" data-user-comments-count=\"1\"\n" +
        "                           data-toggle=\"modal\" data-target=\"#userModal\" href=\"#\"></a>" +
        "<div id=\"userModal\" class=\"modal fade bd-example-modal-sm\" tabindex=\"-1\" role=\"dialog\"\n" +
        "     aria-labelledby=\"exampleModalLabel\" aria-hidden=\"true\">\n" +
        "    <div class=\"modal-dialog modal-sm modal-dialog-centered\">\n" +
        "        <div class=\"modal-content\">\n" +
        "            <div class=\"modal-header\">\n" +
        "                <h5 id=\"user-modal-title\" class=\"modal-title\"></h5> <a sec:authorize=\"isAuthenticated()\"\n" +
        "                                                                       th:if=\"${isAdmin == true}\"\n" +
        "                                                                       id=\"banButton\"\n" +
        "                                                                       href=\"#\"></a>\n" +
        "            </div>\n" +
        "            <div class=\"modal-body\">\n" +
        "                <div class=\"form-group no-bottom-margin\">\n" +
        "                    <h5 id=\"banned\" th:text=\"'(' + #{user.banned} + ')'\"></h5>\n" +
        "                    <b th:text=\"#{games.rating}\"></b><b>:</b> <label\n" +
        "                        id=\"rating-label\"></label><br>\n" +
        "                    <b th:text=\"#{game.user.added}\"></b><b>:</b> <label id=\"games-count-label\"></label> <a\n" +
        "                        id=\"show-all-games-link\">Show All Games</a> <a\n" +
        "                        th:if=\"${isAdmin == true}\" sec:authorize=\"isAuthenticated()\"\n" +
        "                        id=\"delete-all-games-button\"\n" +
        "                        class=\"fas fa-trash-alt\" href=\"#\">Delete All Games</a><br>\n" +
        "                    <b th:text=\"#{game.user.rated}\"></b><b>:</b> <label id=\"rated-count-label\"></label> <a\n" +
        "                        id=\"show-all-rated-link\">Show All Reviews</a> <a\n" +
        "                        th:if=\"${isAdmin == true}\" sec:authorize=\"isAuthenticated()\"\n" +
        "                        id=\"delete-all-reviews-button\" class=\"fas fa-trash-alt\"\n" +
        "                        href=\"#\">Delete All Reviews</a><br>\n" +
        "                    <b th:text=\"#{game.comments}\"></b><b>:</b> <label id=\"comments-count-label\" class=\"no-bottom-margin\"></label> <a\n" +
        "                        id=\"show-all-comments-link\">Show All Comments</a> <a\n" +
        "                        th:if=\"${isAdmin == true}\" sec:authorize=\"isAuthenticated()\"\n" +
        "                        id=\"delete-all-comments-button\" class=\"fas fa-trash-alt\"\n" +
        "                        href=\"#\">Delete All Comments</a>\n" +
        "                </div>\n" +
        "            </div>\n" +
        "            <div class=\"modal-footer\">\n" +
        "                <button th:text=\"#{create.close}\" type=\"button\" class=\"btn btn-light\" data-dismiss=\"modal\"></button>\n" +
        "            </div>\n" +
        "        </div>\n" +
        "    </div>\n" +
        "</div>\n";
}

test('setUpUserModal sets up on show event on user modal', () => {
    setSimpleEventHandler("userModal", "show");

    userModalModule.setUpUserModal();

    expect(testUtils.hasEvent("userModal", "show", 1))
        .toBeTruthy();
    expect(testUtils.hasEventWithNamespace("userModal",
        "show", 0, "bs.modal")).toBeTruthy();
});

test('setUpUserModal sets up that on user modal show, name should be set', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("enabled-user-zero-count-link");

    expect($('#user-modal-title').text())
        .toBe("enabled-user-zero-count-name");
});

test('setUpUserModal sets up that on user modal show, rating should be set', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("enabled-user-zero-count-link");

    expect($('#rating-label').text()).toBe("1.0/5.0");
});

test('setUpUserModal sets up that on user modal show, games count should ' +
    'be set', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");

    expect($('#games-count-label').text()).toBe("1");
});

test('setUpUserModal sets up that on user modal show, reviews count should' +
    ' be set', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");

    expect($('#rated-count-label').text()).toBe("1");
});

test('setUpUserModal sets up that on user modal show, comments count ' +
    'should be set', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");

    expect($('#comments-count-label').text()).toBe("1");
});

test('setUpUserModal sets up that on user modal show, if user enabled, ban ' +
    'button changes to ban mode', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("enabled-user-zero-count-link");

    expect($('#banButton').attr("class")).toBe("fas fa-ban");
});

test('setUpUserModal sets up that on user modal show, if user disabled, ' +
    'ban button changes to unban mode', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");

    expect($('#banButton').attr("class")).toBe("fas fa-undo");
});

test('setUpUserModal sets up that on user modal show, if user enabled and ' +
    'ban button is clicked, alert should be shown', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("enabled-user-zero-count-link");
    clickBanButton();

    expect(testUtils.getVar("showAlertModal")).toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showAlertModal")).toHaveBeenCalledWith(
        BAN_USER_STRING, ARE_YOU_SURE_STRING, expect.any(Function),
        ALERT_MODE.QUESTION);
});

test('setUpUserModal sets up that on user modal show, if user disabled and ' +
    'ban button is clicked, alert should be shown', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");
    clickBanButton();

    expect(testUtils.getVar("showAlertModal")).toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showAlertModal")).toHaveBeenCalledWith(
        UNBAN_USER_STRING, ARE_YOU_SURE_STRING, expect.any(Function),
        ALERT_MODE.QUESTION);
});

test('setUpUserModal sets up that on user modal show, if user enabled and ' +
    'ban button is clicked and alert is confirmed, POST request should be sent', () => {
    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);

    userModalModule.setUpUserModal();

    triggerShowOnUserModal("enabled-user-zero-count-link");
    clickBanButton();

    expect(testUtils.getVar("makeAjaxRequest")).toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledWith(HTTP_REQUEST_TYPE.POST,
            "/api/users/1/ban", null, expect.any(Function));
});

test('setUpUserModal sets up that on user modal show, if user disabled and ' +
    'ban button is clicked and alert is confirmed, DELETE request should be sent', () => {
    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);

    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");
    clickBanButton();

    expect(testUtils.getVar("makeAjaxRequest")).toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledWith(HTTP_REQUEST_TYPE.DELETE,
            "/api/users/2/ban", null, expect.any(Function));
});

test('setUpUserModal sets up that on user modal show, if user enabled and ' +
    'ban button is clicked, alert is confirmed and POST request is succeed, ' +
    'page should be reloaded', () => {
    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);
    testUtils.mockImpl("makeAjaxRequest", testUtils.trueRequestResult);
    const locationMock = testUtils.mockLocation();

    userModalModule.setUpUserModal();

    triggerShowOnUserModal("enabled-user-zero-count-link");
    clickBanButton();

    expect(locationMock.reload).toHaveBeenCalledTimes(1);
});

test('setUpUserModal sets up that on user modal show, if user disabled and ' +
    'ban button is clicked, alert is confirmed and DELETE request is succeed, ' +
    'page should be reloaded', () => {
    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);
    testUtils.mockImpl("makeAjaxRequest", testUtils.trueRequestResult);
    const locationMock = testUtils.mockLocation();

    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");
    clickBanButton();

    expect(locationMock.reload).toHaveBeenCalledTimes(1);
});

test('setUpUserModal sets up that on user modal show, if user enabled and ' +
    'ban button is clicked, alert is confirmed and POST request is not succeed, ' +
    'error modal should be shown', () => {
    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);
    testUtils.mockImpl("makeAjaxRequest", testUtils.falseRequestResult);

    userModalModule.setUpUserModal();

    triggerShowOnUserModal("enabled-user-zero-count-link");
    clickBanButton();

    expect(testUtils.getVar("showSimpleErrorModal"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showSimpleErrorModal"))
        .toHaveBeenCalledWith(CAN_NOT_BAN_STRING);
});

test('setUpUserModal sets up that on user modal show, if user disabled and ' +
    'ban button is clicked, alert is confirmed and DELETE request is not succeed, ' +
    'error modal should be shown', () => {
    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);
    testUtils.mockImpl("makeAjaxRequest", testUtils.falseRequestResult);

    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");
    clickBanButton();

    expect(testUtils.getVar("showSimpleErrorModal"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showSimpleErrorModal"))
        .toHaveBeenCalledWith(CAN_NOT_UNBAN_STRING);
});

test('setUpUserModal sets up that on user modal show, if user enabled, ' +
    'banned label is hidden', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("enabled-user-zero-count-link");

    expect($('#banned').css("display") === 'none').toBeTruthy();
});

test('setUpUserModal sets up that on user modal show, if user disabled, ' +
    'banned label is shown', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");

    expect($('#banned').css("display") !== 'none').toBeTruthy();
});

test('setUpUserModal sets up that on user modal show, if user games ' +
    'count > 0, show all games link href should be set', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");

    expect($('#show-all-games-link').attr("href")).toBe("/games?user=2");
});

test('setUpUserModal sets up that on user modal show, if user games ' +
    'count > 0, on click event should be set on delete all games button', () => {
    setSimpleEventHandler("delete-all-games-button", "click");

    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");

    expect(testUtils.hasEvent("delete-all-games-button", "click",
        1)).toBeTruthy();
});

test('setUpUserModal sets up that on user modal show, if user games ' +
    'count > 0 and delete all games button is clicked, alert should be shown', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");
    clickDeleteAllGamesButton();

    expect(testUtils.getVar("showAlertModal")).toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showAlertModal")).toHaveBeenCalledWith(
        DELETE_ALL_GAMES_STRING, ARE_YOU_SURE_STRING, expect.any(Function),
        ALERT_MODE.QUESTION);
});

test('setUpUserModal sets up that on user modal show, if user games ' +
    'count > 0, delete all games button is clicked and alert is confirmed, ' +
    'DELETE request should be sent', () => {
    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);

    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");
    clickDeleteAllGamesButton();

    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledWith(HTTP_REQUEST_TYPE.DELETE,
            "/api/users/2/games", null, expect.any(Function));
});

test('setUpUserModal sets up that on user modal show, if user games ' +
    'count > 0, delete all games button is clicked and alert is confirmed and ' +
    'DELETE request is succeed, page should be forwarded to origin', () => {
    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);
    testUtils.mockImpl("makeAjaxRequest", testUtils.trueRequestResult);
    const locationMock = testUtils.mockLocation();

    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");
    clickDeleteAllGamesButton();

    expect(locationMock.replace).toHaveBeenCalledTimes(1);
    expect(locationMock.replace).toHaveBeenCalledWith("testOrigin");
});

test('setUpUserModal sets up that on user modal show, if user games ' +
    'count > 0, delete all games button is clicked and alert is confirmed and ' +
    'DELETE request is not succeed, error modal should be shown', () => {
    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);
    testUtils.mockImpl("makeAjaxRequest", testUtils.falseRequestResult);

    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");
    clickDeleteAllGamesButton();

    expect(testUtils.getVar("showSimpleErrorModal"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showSimpleErrorModal"))
        .toHaveBeenCalledWith(CAN_NOT_DELETE_ALL_GAMES_STRING);
});

test('setUpUserModal sets up that on user modal show, if user games ' +
    'count > 0, show all games link should be shown', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");

    expect($('#show-all-games-link').css("display") !== 'none')
        .toBeTruthy();
});

test('setUpUserModal sets up that on user modal show, if user games ' +
    'count > 0, delete all games button should be shown', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");

    expect($('#delete-all-games-button').css("display") !== 'none')
        .toBeTruthy();
});

test('setUpUserModal sets up that on user modal show, if user games ' +
    'count is 0, show all games link should be hidden', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("enabled-user-zero-count-link");

    expect($('#show-all-games-link').css("display") === 'none')
        .toBeTruthy();
});

test('setUpUserModal sets up that on user modal show, if user games ' +
    'count is 0, delete all games button should be hidden', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("enabled-user-zero-count-link");

    expect($('#delete-all-games-button').css("display") === 'none')
        .toBeTruthy();
});

test('setUpUserModal sets up that on user modal show, if user comments ' +
    'count > 0, show all comments link href should be set', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");

    expect($('#show-all-comments-link').attr("href")).toBe("/comments?user=2");
});

test('setUpUserModal sets up that on user modal show, if user comments ' +
    'count > 0, on click event should be set on delete all comments button', () => {
    setSimpleEventHandler("delete-all-comments-button", "click");

    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");

    expect(testUtils.hasEvent("delete-all-comments-button", "click",
        1)).toBeTruthy();
});

test('setUpUserModal sets up that on user modal show, if user comments ' +
    'count > 0 and delete all comments button is clicked, alert should be shown', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");
    clickDeleteAllCommentsButton();

    expect(testUtils.getVar("showAlertModal")).toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showAlertModal")).toHaveBeenCalledWith(
        DELETE_ALL_COMMENTS_STRING, ARE_YOU_SURE_STRING, expect.any(Function),
        ALERT_MODE.QUESTION);
});

test('setUpUserModal sets up that on user modal show, if user comments ' +
    'count > 0, delete all comments button is clicked and alert is confirmed, ' +
    'DELETE request should be sent', () => {
    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);

    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");
    clickDeleteAllCommentsButton();

    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledWith(HTTP_REQUEST_TYPE.DELETE,
            "/api/users/2/comments", null, expect.any(Function));
});

test('setUpUserModal sets up that on user modal show, if user comments ' +
    'count > 0, delete all comments button is clicked and alert is confirmed and ' +
    'DELETE request is succeed, page should be reloaded', () => {
    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);
    testUtils.mockImpl("makeAjaxRequest", testUtils.trueRequestResult);
    const locationMock = testUtils.mockLocation();

    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");
    clickDeleteAllCommentsButton();

    expect(locationMock.reload).toHaveBeenCalledTimes(1);
});

test('setUpUserModal sets up that on user modal show if user comments ' +
    'count > 0, delete all comments button is clicked and alert is confirmed and ' +
    'DELETE request is not succeed, error modal should be shown', () => {
    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);
    testUtils.mockImpl("makeAjaxRequest", testUtils.falseRequestResult);

    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");
    clickDeleteAllCommentsButton();

    expect(testUtils.getVar("showSimpleErrorModal"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showSimpleErrorModal"))
        .toHaveBeenCalledWith(CAN_NOT_DELETE_ALL_COMMENTS_STRING);
});

test('setUpUserModal sets up that on user modal show if user comments ' +
    'count > 0, show all comments link should be shown', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");

    expect($('#show-all-comments-link').css("display") !== 'none')
        .toBeTruthy();
});

test('setUpUserModal sets up that on user modal show, if user ' +
    'comments count > 0, delete all comments button should be shown', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");

    expect($('#delete-all-comments-button').css("display") !== 'none')
        .toBeTruthy();
});

test('setUpUserModal sets up that on user modal show, if user comments' +
    'count is 0, show all comments link should be hidden', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("enabled-user-zero-count-link");

    expect($('#show-all-comments-link').css("display") === 'none')
        .toBeTruthy();
});

test('setUpUserModal sets up that on user modal show, if user comments ' +
    'count is 0, delete all comments button should be hidden', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("enabled-user-zero-count-link");

    expect($('#delete-all-comments-button').css("display") === 'none')
        .toBeTruthy();
});

test('setUpUserModal sets up that on user modal show, if user reviews ' +
    'count > 0, on click event should be set on delete all reviews button', () => {
    setSimpleEventHandler("delete-all-reviews-button", "click");

    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");

    expect(testUtils.hasEvent("delete-all-reviews-button", "click",
        1)).toBeTruthy();
});

test('setUpUserModal sets up that on user modal show, if user reviews ' +
    'count > 0 and delete all reviews button is clicked, alert should be shown', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");
    clickDeleteAllReviewsButton();

    expect(testUtils.getVar("showAlertModal")).toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showAlertModal")).toHaveBeenCalledWith(
        DELETE_ALL_RATED_STRING, ARE_YOU_SURE_STRING, expect.any(Function),
        ALERT_MODE.QUESTION);
});

test('setUpUserModal sets up that on user modal show, if user reviews ' +
    'count > 0, delete all reviews button is clicked and alert is confirmed, ' +
    'DELETE request should be sent', () => {
    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);

    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");
    clickDeleteAllReviewsButton();

    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledWith(HTTP_REQUEST_TYPE.DELETE,
            "/api/users/2/reviews", null, expect.any(Function));
});

test('setUpUserModal sets up that on user modal show, if user reviews ' +
    'count > 0, delete all reviews button is clicked, alert is confirmed and ' +
    'DELETE request is succeed, page should be reloaded', () => {
    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);
    testUtils.mockImpl("makeAjaxRequest", testUtils.trueRequestResult);
    const locationMock = testUtils.mockLocation();

    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");
    clickDeleteAllReviewsButton();

    expect(locationMock.reload).toHaveBeenCalledTimes(1);
});

test('setUpUserModal sets up that on user modal show, if user reviews ' +
    'count > 0, delete all reviews button is clicked, alert is confirmed and ' +
    'DELETE request is not succeed, error modal should be shown', () => {
    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);
    testUtils.mockImpl("makeAjaxRequest", testUtils.falseRequestResult);

    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");
    clickDeleteAllReviewsButton();

    expect(testUtils.getVar("showSimpleErrorModal"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showSimpleErrorModal"))
        .toHaveBeenCalledWith(CAN_NOT_DELETE_ALL_RATED_STRING);
});

test('setUpUserModal sets up that on user modal show, if user reviews ' +
    'count > 0, delete all reviews button should be shown', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("disabled-user-one-count-link");

    expect($('#delete-all-reviews-button').css("display") !== 'none')
        .toBeTruthy();
});

test('setUpUserModal sets up that on user modal show, if user reviews ' +
    'count is 0, delete all reviews button should be hidden', () => {
    userModalModule.setUpUserModal();

    triggerShowOnUserModal("enabled-user-zero-count-link");

    expect($('#delete-all-reviews-button').css("display") === 'none')
        .toBeTruthy();
});

function setSimpleEventHandler(id, eventName) {
    $('#' + id).off().on(eventName, function (event) {
        console.log(event + ' does nothing');
    });
}

function triggerShowOnUserModal(relatedTarget) {
    const event = $.Event("show.bs.modal");

    event.relatedTarget = $("#" + relatedTarget);

    $("#userModal").trigger(event);
}

function clickBanButton() {
    $('#banButton').trigger('click');
}

function clickDeleteAllGamesButton() {
    $('#delete-all-games-button').trigger('click');
}

function clickDeleteAllCommentsButton() {
    $('#delete-all-comments-button').trigger('click');
}

function clickDeleteAllReviewsButton() {
    $('#delete-all-reviews-button').trigger('click');
}

