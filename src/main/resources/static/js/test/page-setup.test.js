/**
 * @jest-environment jsdom
 */

const pageSetupModule = require("../page-setup");
const testUtils = require("./util/test-utils.js")

beforeAll(() => {
    setUpJquery();
    setUpGlobalVariables();
    setUpHtml();
});

function setUpJquery() {
    global.$ = require("jquery");
    global.jQuery = global.$;
}

function setUpGlobalVariables() {
    global.HTTP_REQUEST_TYPE = {
        POST: "POST",
        PUT: "PUT",
        DELETE: "DELETE",
    }
}

function setUpHtml() {
    document.body.innerHTML =
        "        <nav class=\"nav nav-masthead justify-content-center float-md-end\">\n" +
        "            <a id=\"play-navbar\" th:text=\"#{navbar.play}\" class=\"nav-link\" href=\"#\"></a>\n" +
        "            <a id=\"create-navbar\" th:text=\"#{navbar.create}\" class=\"nav-link\" href=\"#\"></a>\n" +
        "            <label id=\"delimiter\" class=\"nav-delimiter\">|</label>\n" +
        "            <div class=\"dropdown\">\n" +
        "            <a id=\"user-navbar\" th:text=\"#{navbar.sign-in}\" type=\"button\"\n" +
        "               class=\"nav-link btn-group\" aria-haspopup=\"true\" aria-expanded=\"false\" href=\"#\"></a>\n" +
        "                <div class=\"dropdown-menu\" aria-labelledby=\"user-navbar\">\n" +
        "                    <a id=\"favoritesDropdown\" th:text=\"#{navbar.favorites}\" class=\"dropdown-item\" href=\"#\"></a>\n" +
        "                    <a id=\"yourGamesDropdown\" th:text=\"#{navbar.your-games}\" class=\"dropdown-item\" href=\"#\"></a>\n" +
        "                    <a id=\"editProfileDropdown\" th:text=\"#{navbar.edit-profile}\" class=\"dropdown-item\" href=\"#\"></a>\n" +
        "                </div>\n" +
        "            </div>\n" +
        "                <a id=\"logout-navbar\" th:text=\"#{navbar.sign-out}\" class=\"nav-link\" href=\"#\"></a>\n" +
        "        </nav>\n";
}

test('setTitle sets page title', () => {
    pageSetupModule.setTitle("testTitle");

    expect($(document).attr("title")).toEqual("testTitle");
});

test('setActiveMenu sets current active menu', () => {
    pageSetupModule.setActiveMenu("play-navbar");

    const playNavbar = $('#play-navbar');

    expect(playNavbar.attr("aria-current")).toEqual("page");
    expect(playNavbar.hasClass("nav-link")).toBeTruthy();
    expect(playNavbar.hasClass("active")).toBeTruthy();
    expect(playNavbar.hasClass("btn-group")).toBeTruthy();
});

test('setActiveMenu sets current active menu for user-navbar', () => {
    pageSetupModule.setActiveMenu("user-navbar");

    const userNavbar = $('#user-navbar');

    expect(userNavbar.attr("aria-current")).toEqual("page");
    expect(userNavbar.hasClass("nav-link")).toBeTruthy();
    expect(userNavbar.hasClass("active")).toBeTruthy();
    expect(userNavbar.hasClass("btn-group")).toBeTruthy();
    expect(userNavbar.hasClass("profile-link")).toBeTruthy();
});

test('logout makes POST request', () => {
    const makeAjaxRequestMock = testUtils.mockFunc("makeAjaxRequest");

    pageSetupModule.logout();

    expect(makeAjaxRequestMock).toHaveBeenCalledTimes(1);
    expect(makeAjaxRequestMock).toHaveBeenCalledWith(HTTP_REQUEST_TYPE.POST,
        "/logout", null, expect.any(Function));
});

test('logout reloads page after successful POST result', () => {
    testUtils.mockImpl("makeAjaxRequest",
        function trueRequestResult(type, url, data, func) {
            func(data);
        }
    );
    const locationMock = testUtils.mockLocation();

    pageSetupModule.logout();

    expect(locationMock.reload).toHaveBeenCalledTimes(1);
    expect(locationMock.reload).toHaveBeenCalledWith("testOrigin");
});