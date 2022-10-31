/**
 * @jest-environment jsdom
 */

const initGamePageModule = require("../game-info-setup");
const testUtils = require("./util/test-utils.js")

beforeAll(() => {
    setUpGlobalVariables();
    setUpJquery();
    setUpMocks();
    setUpHtml();
});

function setUpJquery() {
    global.$ = require("jquery");
    global.jQuery = global.$;
}

function setUpGlobalVariables() {
    global.CANNOT_SEND_THE_REVIEW_STRING = "CANNOT_SEND_THE_REVIEW_STRING";

    global.HTTP_REQUEST_TYPE = {
        POST: "POST",
        PUT: "PUT",
        DELETE: "DELETE",
    }
}

function setUpMocks() {
    jest.spyOn($.fn, 'off');
    jest.spyOn($.fn, 'on');
    $.fn.rating = jest.fn();
    jest.spyOn($.fn, 'rating');

    testUtils.mockFuncs("makeAjaxRequest", "showSimpleErrorModal");
}

function setUpHtml() {
    document.body.innerHTML =
        "        <div class=\"card\">\n" +
        "            <div class=\"card-body\">\n" +
        "                <label th:if=\"${game == null}\" th:text=\"#{games.nothing-found}\"></label>\n" +
        "                <div th:if=\"${game}\">\n" +
        "                    <h5 class=\"card-title\"><label th:text=\"${game.name}\"></label> <span class=\"badge bg-secondary\"\n" +
        "                                                                                            th:text=\"${game.language}\"></span>\n" +
        "                        <a sec:authorize=\"isAuthenticated()\"\n" +
        "                           th:attr=\"data-game-id=${game.id}\"\n" +
        "                           data-condition=\"unfavorited\" class=\"far fa-star\" href=\"#\"></a>\n" +
        "                        <a sec:authorize=\"isAuthenticated()\" th:if=\"${game.userId == userId or isAdmin == true}\"\n" +
        "                           id=\"game-edit\" data-game-id=\"TestGameId\" class=\"fas fa-edit\" href=\"#\"></a>\n" +
        "                        <a sec:authorize=\"isAuthenticated()\" th:if=\"${game.userId == userId or isAdmin == true}\"\n" +
        "                           th:attr=\"onclick=|deleteGame('${game.id}')|\" class=\"far fa-trash-alt\" href=\"#\" ></a>\n" +
        "                    </h5>\n" +
        "                    <p class=\"card-text\">\n" +
        "                        <label th:text=\"${game.description}\" class=\"card-text\"></label></p>\n" +
        "                    <div th:attr=\"data-game-id=${game.id}\" class=\"rating\"></div>\n" +
        "                    <p class=\"card-text\">\n" +
        "                        <a th:text=\"${game.userName}\"\n" +
        "                           th:attr=\"data-user-name=${game.userName}, data-user-id=${game.userId}\"\n" +
        "                           data-toggle=\"modal\" data-target=\"#userModal\" href=\"#\"></a>\n" +
        "                    </p>\n" +
        "                    <a id=\"play-button\"  th:text=\"#{navbar.play}\" class=\"btn btn-light\" href=\"#\"></a>\n" +
        "                    <a sec:authorize=\"isAuthenticated()\" th:text=\"#{game.rate}\"\n" +
        "                       class=\"btn btn-light\" data-toggle=\"modal\" data-target=\"#ratingModal\" href=\"#\"></a>\n" +
        "                    <p>\n" +
        "                    <div class=\"shareon\">\n" +
        "                        <a class=\"facebook\"></a>\n" +
        "                        <button class=\"twitter\"></button>\n" +
        "                        <button class=\"reddit\"></button>\n" +
        "                        <button class=\"telegram\"></button>\n" +
        "                        <button class=\"vkontakte\"></button>\n" +
        "                    </div>\n" +
        "                    </p>\n" +
        "\n" +
        "                    <hr/>\n" +
        "\n" +
        "                    <h5 class=\"card-title\"><label th:text=\"#{game.comments}\"></label></h5>\n" +
        "\n" +
        "                    <div sec:authorize=\"!isAuthenticated()\">\n" +
        "                        <label th:text=\"#{game.to-write-a-comment}\"></label> <a\n" +
        "                            th:text=\"#{game.login}\" th:href=\"${endpoints.LOGIN_ENDPOINT}\"\n" +
        "                            style=\"text-decoration: underline\"></a> <label\n" +
        "                            th:text=\"#{game.or}\"></label> <a th:text=\"#{game.register}\"\n" +
        "                                                             th:href=\"${endpoints.REGISTER_ENDPOINT}\"\n" +
        "                                                             style=\"text-decoration: underline\"></a><br></div>\n" +
        "\n" +
        "                    <div th:if=\"${game}\"\n" +
        "                         sec:authorize=\"isAuthenticated()\">\n" +
        "                        <textarea name=\"comment\" class=\"form-control\"></textarea>\n" +
        "                        <div id=\"comment-error-div\" hidden=\"true\">\n" +
        "                            <label id=\"comment-error\" class=\"error-text\"></label></div>\n" +
        "\n" +
        "                        <input id=\"send-comment-button\" th:value=\"#{game.send}\" class=\"btn btn-light comments-send\" type=\"submit\">\n" +
        "                    </div>\n" +
        "\n" +
        "                    <div th:replace=\"fragments/comments.html::comments\"></div>\n" +
        "\n" +
        "                    <hr th:if=\"${not #lists.isEmpty(comments)}\"/>\n" +
        "\n" +
        "                    <small th:text=\"${#dates.format(game.date, 'dd.MM.yyyy')}\"\n" +
        "                           class=\"text-muted float-right\"></small>\n" +
        "                </div>\n" +
        "            </div>\n" +
        "    <div th:if=\"${game}\" id=\"ratingModal\" class=\"modal fade bd-example-modal-sm\" tabindex=\"-1\" role=\"dialog\"\n" +
        "         aria-labelledby=\"exampleModalLabel\" aria-hidden=\"true\">\n" +
        "        <div class=\"modal-dialog modal-sm modal-dialog-centered\">\n" +
        "            <div class=\"modal-content\">\n" +
        "                <div class=\"modal-header\">\n" +
        "                    <h5 class=\"modal-title\" th:text=\"#{game.rate}\"></h5>\n" +
        "                </div>\n" +
        "                <div class=\"modal-body\">\n" +
        "                    <div sec:authorize=\"isAuthenticated()\" id=\"rate\"\n" +
        "                         th:attr=\"data-id=${game.id}\" class=\"review\"></div>\n" +
        "                </div>\n" +
        "                <div class=\"modal-footer\">\n" +
        "                    <button th:text=\"#{create.close}\" type=\"button\" class=\"btn btn-light\"\n" +
        "                            data-dismiss=\"modal\"></button>\n" +
        "                </div>\n" +
        "            </div>\n" +
        "        </div>\n" +
        "    </div>\n";
}

test('setUpRateModal sets listeners', () => {
    setSimpleEventHandler("ratingModal", "show");

    initGamePageModule.setUpRateModal(1, 5);

    expect(testUtils.hasEvent("ratingModal", "show", 1))
        .toBeTruthy();
    expect(testUtils.hasEventWithNamespace("ratingModal",
        "show", 0, "bs.modal")).toBeTruthy();
});

test('setUpRateModal sets that on rating modal show rating stars ' +
    ' should be called', () => {
    initGamePageModule.setUpRateModal(1, 5);

    triggerShowOnRatingModal();

    expect($().rating).toHaveBeenCalledTimes(1);
    expect($().rating).toHaveBeenCalledWith({
        "color": "#FFF",
        "value": 5,
        "click": expect.any(Function)
    });
});

test('setReview sets that on rate modal show, click to rating stars' +
    ' should make POST request if user review is 0', () => {
    initGamePageModule.setUpRateModal(1, 0);

    triggerShowOnRatingModal();

    clickOnRatingWithStars(5);

    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("makeAjaxRequest")).toHaveBeenCalledWith(
        HTTP_REQUEST_TYPE.POST, "/api/games/1/rate", {rating: 5},
        expect.any(Function));
});

test('setReview sets that on rate modal show, click to rating stars' +
    ' should make PUT request if user review is not equal to ' +
    ' number of stars clicked', () => {
    initGamePageModule.setUpRateModal(1, 4);

    triggerShowOnRatingModal();

    clickOnRatingWithStars(5);

    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("makeAjaxRequest")).toHaveBeenCalledWith(
        HTTP_REQUEST_TYPE.PUT, "/api/games/1/rate", {rating: 5},
        expect.any(Function));
});

test('setReview sets that on rate modal show, click to rating stars' +
    ' should not make HTTP request if user review is equal to ' +
    ' number of stars clicked', () => {
    initGamePageModule.setUpRateModal(1, 4);

    triggerShowOnRatingModal();

    clickOnRatingWithStars(4);

    expect(testUtils.getVar("makeAjaxRequest"))
        .toHaveBeenCalledTimes(0);
});

test('setReview sets that on rate modal show, after click to rating stars' +
    ' and successful HTTP request page should be reloaded', () => {
    testUtils.mockImpl("makeAjaxRequest", testUtils.trueRequestResult);
    const locationMock = testUtils.mockLocation();

    initGamePageModule.setUpRateModal(1, 4);

    triggerShowOnRatingModal();

    clickOnRatingWithStars(5);

    expect(locationMock.reload).toHaveBeenCalledTimes(1);
});

test('setReview sets that on rate modal show, after click to rating stars' +
    ' and failure HTTP request error should be shown', () => {
    testUtils.mockImpl("makeAjaxRequest", testUtils.falseRequestResult);

    initGamePageModule.setUpRateModal(1, 4);

    triggerShowOnRatingModal();

    clickOnRatingWithStars(5);

    expect(testUtils.getVar("showSimpleErrorModal"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("showSimpleErrorModal")).toHaveBeenCalledWith(
        CANNOT_SEND_THE_REVIEW_STRING);
});

function setSimpleEventHandler(id, eventName) {
    $('#' + id).off().on(eventName, function (event) {
        console.log(event + ' does nothing');
    });
}

function triggerShowOnRatingModal() {
    const event = $.Event("show.bs.modal");
    $("#ratingModal").trigger(event);
}

function clickOnRatingWithStars(stars) {
    const ratingCall = $().rating.mock.calls[0][0];

    ratingCall.click({
        stars: stars
    });
}