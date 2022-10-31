/**
 * @jest-environment jsdom
 */

const initCommentsModule = require("../comments");
const testUtils = require("./util/test-utils.js")

beforeAll(() => {
    setUpJquery();
    setUpGlobalVariables();
    setUpMocks();
    setUpHtml();
});

function setUpJquery() {
    global.$ = require("jquery");
    global.jQuery = global.$;
}

function setUpGlobalVariables() {
    global.DELETE_COMMENT_STRING = "DELETE_COMMENT_STRING";
    global.ARE_YOU_SURE_STRING = "ARE_YOU_SURE_STRING";
    global.CANNOT_DELETE_THE_COMMENT_STRING = "CANNOT_DELETE_THE_COMMENT_STRING";
    global.DELETE_COMMENT_STRING = "DELETE_COMMENT_STRING";
    global.DELETE_COMMENT_STRING = "DELETE_COMMENT_STRING";

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
    testUtils.mockFuncs("makeAjaxRequest", "showAlertModal",
        "showSimpleErrorModal");
}

function setUpHtml() {
    document.body.innerHTML =
        "                    <div>\n" +
        "                        <textarea name=\"comment\" class=\"form-control\"></textarea>\n" +
        "                        <div id=\"comment-error-div\" hidden=\"true\">\n" +
        "                            <label id=\"comment-error\" class=\"error-text\"></label></div>\n" +
        "\n" +
        "                        <input id=\"send-comment-button\" data-game-id=\"1\" th:value=\"#{game.send}\" class=\"btn btn-light comments-send\" type=\"submit\">\n" +
        "                    </div>\n" +
        "           <a id=\"link\" data-text=\"TestCommentText\" data-comment-id=\"2\" data-game-id=\"1\"\n" +
        "           data-toggle=\"modal\" data-target=\"#userModal\" href=\"#\"></a>" +
        "               <p id=\"commentTextTestCommentId\">TestCommentText</p>\n" +
        "            <div class=\"modal-header\">\n" +
        "                <h5 th:text=\"#{game.comments.edit}\" class=\"modal-title\"></h5>\n" +
        "                <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\">\n" +
        "                    <span aria-hidden=\"true\">&times;</span>\n" +
        "                </button>\n" +
        "            </div>\n" +
        "            <div class=\"modal-body\">\n" +
        "                <form>\n" +
        "                    <div class=\"form-group\">\n" +
        "                        <textarea id=\"comment-text\" class=\"form-control\"></textarea>\n" +
        "                    </div>\n" +
        "                </form>\n" +
        "                <div id=\"comment-error-div-modal\" hidden=\"true\">\n" +
        "                    <label id=\"comment-error-modal\"></label></div>\n" +
        "            </div>\n" +
        "            <div class=\"modal-footer\">\n" +
        "                <button id=\"modal-button-save\" th:text=\"#{create.save}\"\n" +
        "                        type=\"button\"  class=\"btn btn-primary\" data-dismiss=\"modal\">\n" +
        "                </button>\n" +
        "                <button th:text=\"#{create.close}\" type=\"button\"\n" +
        "                        class=\"btn btn-secondary\" data-dismiss=\"modal\">\n" +
        "                </button>\n" +
        "            </div>\n" +
        "<div id=\"editCommentModal\" class=\"modal fade\" tabindex=\"-1\" role=\"dialog\" aria-labelledby=\"exampleModalLabel\"\n" +
        "     aria-hidden=\"true\">\n" +
        "    <div class=\"modal-dialog modal-dialog-centered\" role=\"document\">\n" +
        "        <div class=\"modal-content\">\n" +
        "            <div class=\"modal-header\">\n" +
        "                <h5 class=\"modal-title\"></h5>\n" +
        "                <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\">\n" +
        "                    <span aria-hidden=\"true\">&times;</span>\n" +
        "                </button>\n" +
        "            </div>\n" +
        "            <div class=\"modal-body\">\n" +
        "                <form>\n" +
        "                    <div class=\"form-group no-bottom-margin\">\n" +
        "                        <textarea id=\"comment-text\" class=\"form-control\"></textarea>\n" +
        "                    </div>\n" +
        "                </form>\n" +
        "                <div id=\"comment-error-div-modal\" hidden=\"true\">\n" +
        "                    <label id=\"comment-error-modal\" class=\"error-text\"></label></div>\n" +
        "            </div>\n" +
        "            <div class=\"modal-footer\">\n" +
        "                <button id=\"modal-button-save\" th:text=\"#{create.save}\"\n" +
        "                        type=\"button\" class=\"btn btn-light\">\n" +
        "                </button>\n" +
        "                <button th:text=\"#{create.close}\" type=\"button\"\n" +
        "                        class=\"btn btn-secondary\" data-dismiss=\"modal\">\n" +
        "                </button>\n" +
        "            </div>\n" +
        "        </div>\n" +
        "    </div>\n" +
        "</div>";
}

test('setUpCreatingComments removes onclick events and adds new on click ' +
    'event to send comment button', () => {
    testUtils.setSimpleEventHandler("send-comment-button", "click");

    initCommentsModule.setUpCreatingComments();

    expect(testUtils.hasEvent("send-comment-button", "click",
        1)).toBe(true);
});

test('setUpCreatingComments clears constraint errors when send comment ' +
    'button is clicked', () => {
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithFieldError);

    initCommentsModule.setUpCreatingComments();

    createComment("SuccessTest");

    clickSendCommentButton();

    testUtils.mockFunc("makeAjaxRequest");

    clickSendCommentButton();

    expect(testUtils.hasAttr("comment-error-div", "hidden"))
        .toBe(true);
    expect($("#comment-error").text()).toEqual("");
});

test('setUpCreatingComments calls makeAjaxRequest when send comment ' +
    'button is clicked', () => {
    initCommentsModule.setUpCreatingComments();

    createComment("SuccessTest");

    clickSendCommentButton();

    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledWith(HTTP_REQUEST_TYPE.POST, "/api/games/1/comments",
            {text: "SuccessTest"}, expect.any(Function));
});

test('setUpCreatingComments calls makeAjaxRequest with trimmed text ' +
    'when send comment button is clicked', () => {
    initCommentsModule.setUpCreatingComments();

    createComment("   Text   ");

    clickSendCommentButton();

    expect(testUtils.getVar("makeAjaxRequest").mock.calls[0][2].text)
        .toEqual("Text");
});

test('setUpCreatingComments reloads page after success post request ' +
    'when send comment button is clicked', () => {
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithoutFieldError);
    const locationMock = testUtils.mockLocation();

    initCommentsModule.setUpCreatingComments();

    createComment("SuccessTest");

    clickSendCommentButton();

    expect(locationMock.reload).toHaveBeenCalledTimes(1);
});

test('setUpCreatingComments shows error after failure post request ' +
    'when send comment button is clicked', () => {
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithFieldError);

    initCommentsModule.setUpCreatingComments();

    createComment("SuccessTest");

    clickSendCommentButton();

    expect(testUtils.hasAttr("comment-error-div", "hidden"))
        .toBe(false);
    expect($("#comment-error").text()).toEqual("TestError");
});

test('setUpEditingComments adds on show listener to modal', () => {
    testUtils.setSimpleEventHandler("editCommentModal", "show");

    initCommentsModule.setUpEditingComments();

    expect(testUtils.hasEvent("editCommentModal", "show", 1))
        .toBeTruthy();
    expect(testUtils.hasEventWithNamespace("editCommentModal",
        "show", 0, "bs.modal")).toBeTruthy();
});

test('setUpEditingComments sets comment in edit comment modal text area', () => {
    initCommentsModule.setUpEditingComments();

    triggerEditCommentModal();

    expect($('#comment-text').val()).toEqual("TestCommentText");
});

test('setUpEditingComments adds onclick event to save button', () => {
    testUtils.setSimpleEventHandler("editCommentModal", "click");

    initCommentsModule.setUpEditingComments();

    triggerEditCommentModal();

    expect(testUtils.hasEvent("modal-button-save", "click", 1))
        .toBeTruthy();
});

test('setUpEditingComments clears constraints errors when save button ' +
    'is clicked', () => {
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithFieldError);

    initCommentsModule.setUpEditingComments();

    triggerEditCommentModal();

    editComment("SuccessTest");

    clickSaveCommentButton();

    testUtils.mockFunc("makeAjaxRequest");

    clickSaveCommentButton();

    expect(testUtils.hasAttr("comment-error-div-modal", "hidden"))
        .toBe(true);
    expect($("#comment-error-modal").text()).toEqual("");
});

test('setUpEditingComments calls to makeAjaxRequest when save button is ' +
    'clicked and edited text not equals to original text', () => {
    initCommentsModule.setUpEditingComments();

    triggerEditCommentModal();

    editComment("SuccessTest");

    clickSaveCommentButton();

    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledWith(HTTP_REQUEST_TYPE.PUT, "/api/games/1/comments/2",
            {text: "SuccessTest"}, expect.any(Function));
});

test('setUpEditingComments calls makeAjaxRequest with trimmed text' +
    'when save button is clicked', () => {
    initCommentsModule.setUpEditingComments();

    triggerEditCommentModal();

    editComment("   Text   ");

    clickSaveCommentButton();

    expect(testUtils.getVar("makeAjaxRequest").mock.calls[0][2].text)
        .toEqual("Text");
});

test('setUpEditingComments reloads page if PUT request is succeed', () => {
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithoutFieldError);
    const location = testUtils.mockLocation();

    initCommentsModule.setUpEditingComments();

    triggerEditCommentModal();

    editComment("SuccessTest");

    clickSaveCommentButton();

    expect(location.reload).toHaveBeenCalledTimes(1);
});

test('setUpEditingComments shows error if PUT request is failed', () => {
    testUtils.mockImpl("makeAjaxRequest",
        testUtils.requestResultWithFieldError);

    initCommentsModule.setUpEditingComments();

    triggerEditCommentModal();

    editComment("SuccessTest");

    clickSaveCommentButton();

    expect(testUtils.hasAttr("comment-error-div-modal", "hidden"))
        .toBe(false);
    expect($("#comment-error-modal").text()).toEqual("TestError");
});

test('deleteComment calls to showAlertModal', () => {
    initCommentsModule.deleteComment(1, 2);

    expect(testUtils.getVar("showAlertModal")).toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showAlertModal")).toHaveBeenCalledWith(
        DELETE_COMMENT_STRING, ARE_YOU_SURE_STRING, expect.any(Function),
        ALERT_MODE.QUESTION);
});

test('deleteComment calls to makeAjaxRequest if alert modal is confirmed', () => {
    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);

    initCommentsModule.deleteComment(1, 2);

    expect(testUtils.getVar("makeAjaxRequest")).toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledWith(HTTP_REQUEST_TYPE.DELETE,
            "/api/games/2/comments/1", null, expect.any(Function));
});

test('deleteComment reloads page if DELETE request is succeed', () => {
    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);
    testUtils.mockImpl("makeAjaxRequest", testUtils.trueRequestResult);
    const locationMock = testUtils.mockLocation();

    initCommentsModule.deleteComment(1, 2);

    expect(locationMock.reload).toHaveBeenCalledTimes(1);
});

test('deleteComment() shows error if DELETE request if failed', () => {
    testUtils.mockImpl("showAlertModal",
        testUtils.mockAlertModalThatJustRunsFunc);
    testUtils.mockImpl("makeAjaxRequest", testUtils.falseRequestResult);

    initCommentsModule.deleteComment(1, 2);

    expect(testUtils.getVar("showSimpleErrorModal"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showSimpleErrorModal"))
        .toHaveBeenCalledWith(CANNOT_DELETE_THE_COMMENT_STRING);
});

function triggerEditCommentModal() {
    const event = $.Event("show.bs.modal");
    event.relatedTarget = $("#link");
    $("#editCommentModal").trigger(event);
}

function createComment(text) {
    $('textarea[name="comment"]').val(text);
}

function clickSendCommentButton() {
    $('#send-comment-button').trigger('click');
}

function editComment(text) {
    $('#comment-text').val(text);
}

function clickSaveCommentButton() {
    $('#modal-button-save').trigger('click');
}

