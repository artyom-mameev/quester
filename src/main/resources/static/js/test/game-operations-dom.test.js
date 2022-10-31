/**
 * @jest-environment jsdom
 */

const gameOperations_DomModule = require("../game-operations-dom");
const testUtils = require("./util/test-utils.js")

beforeAll(() => {
    setUpJquery();
    setUpMocks();
    setUpHtml();
});

function setUpJquery() {
    global.$ = require("jquery");
    global.jQuery = global.$;
}

function setUpMocks() {
    testUtils.mockFuncs("favorite", "unfavorite");
}

function setUpHtml() {
    document.body.innerHTML =
        "            <div class=\"col\">\n" +
        "                <div class=\"card\" style=\"width: 18rem;\" >\n" +
        "                    <div class=\"card-body\">\n" +
        "                        <h5 class=\"card-title\"><a href=\"#\" id=\"text-game\" data-game-id=\"TestGameId1\" class=\"game-name\">TestGameName</a>\n" +
        "                            <span th:text=\"${game.language}\" class=\"badge bg-secondary\"></span>\n" +
        "                            <a id=\"favorite-button-1\" data-game-id=\"TestGameId1\"\n" +
        "                               class=\"far fa-star\" href=\"#\"></a>\n" +
        "                            <a sec:authorize=\"isAuthenticated()\" th:if=\"${game.userId == userId or isAdmin == true}\"\n" +
        "                               th:attr=\"data-game-id=${game.id}\" class=\"fas fa-edit\" id=\"game-edit\" href=\"#\"></a>\n" +
        "                            <a sec:authorize=\"isAuthenticated()\" th:if=\"${game.userId == userId or isAdmin == true}\"\n" +
        "                               th:attr=\"onclick=|deleteGame('${game.id}')|\" class=\"far fa-trash-alt\" href=\"#\"></a>\n" +
        "                        </h5>\n" +
        "                        <p class=\"card-text\"><label th:text=\"${game.description}\" class=\"card-text\"></label></p>\n" +
        "                        <div class=\"rating\"\n" +
        "                             data-game-id=\"TestGameId1\"></div>\n" +
        "                        <p class=\"card-text float-right\"><a th:attr=\"data-game-id=${game.id}\" id=\"comments-count\" class=\"comments-count\" href=\"#\"></a><br>\n" +
        "                            <small th:text=\"${#dates.format(game.date, 'dd.MM.yyyy')}\"\n" +
        "                                   class=\"text-muted float-right\"></small></p>\n" +
        "                    </div>\n" +
        "                </div>\n" +
        "            </div>\n" +
        "            <div class=\"col\" th:each=\"game: ${games}\">\n" +
        "                <div class=\"card\" style=\"width: 18rem;\" >\n" +
        "                    <div class=\"card-body\">\n" +
        "                        <h5 class=\"card-title\"><a href=\"#\" th:attr=\"data-game-id=${game.id}\" th:text=\"${game.name}\" class=\"game-name\"></a>\n" +
        "                            <span th:text=\"${game.language}\" class=\"badge bg-secondary\"></span>\n" +
        "                            <a id=\"favorite-button-2\" data-game-id=\"TestGameId2\"\n" +
        "                               class=\"far fa-star\" href=\"#\"></a>\n" +
        "                            <a sec:authorize=\"isAuthenticated()\" th:if=\"${game.userId == userId or isAdmin == true}\"\n" +
        "                               th:attr=\"data-game-id=${game.id}\" class=\"fas fa-edit\" id=\"game-edit\" href=\"#\"></a>\n" +
        "                            <a sec:authorize=\"isAuthenticated()\" th:if=\"${game.userId == userId or isAdmin == true}\"\n" +
        "                               th:attr=\"onclick=|deleteGame('${game.id}')|\" class=\"far fa-trash-alt\" href=\"#\"></a>\n" +
        "                        </h5>\n" +
        "                        <p class=\"card-text\"><label th:text=\"${game.description}\" class=\"card-text\"></label></p>\n" +
        "                        <div class=\"rating\"\n" +
        "                             data-game-id=\"TestGameId2\"></div>\n" +
        "                        <p class=\"card-text float-right\"><a th:attr=\"data-game-id=${game.id}\" id=\"comments-count\" class=\"comments-count\" href=\"#\"></a><br>\n" +
        "                            <small th:text=\"${#dates.format(game.date, 'dd.MM.yyyy')}\"\n" +
        "                                   class=\"text-muted float-right\"></small></p>\n" +
        "                    </div>\n" +
        "                </div>\n" +
        "            </div>\n";
}

test('changeToFavoriteState changes class of favorite button', () => {
    gameOperations_DomModule.changeToFavoriteState(1);

    const favButton1 = $('#favorite-button-1');

    expect(favButton1.hasClass("far")).toBeFalsy();
    expect(favButton1.hasClass("fas")).toBeTruthy();
});

test('changeToFavoriteState makes that on click on favorite button' +
    ' unfavorite function should be called', () => {
    gameOperations_DomModule.changeToFavoriteState(1);

    $('#favorite-button-1').click();

    expect(testUtils.getVar("unfavorite"))
        .toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("unfavorite")).toHaveBeenCalledWith(1);
});

test('changeToUnfavoriteState changes class of favorite button', () => {
    gameOperations_DomModule.changeToUnfavoriteState(1);

    const favButton1 = $('#favorite-button-1');

    expect(favButton1.hasClass("far")).toBeTruthy();
    expect(favButton1.hasClass("fas")).toBeFalsy();
});

test('changeToUnfavoriteState makes that on click on favorite button' +
    ' favorite function should be called', () => {
    gameOperations_DomModule.changeToUnfavoriteState(1);

    $('#favorite-button-1').click();

    expect(testUtils.getVar("favorite")).toHaveBeenCalledTimes(1);
    expect(testUtils.getVar("favorite")).toHaveBeenCalledWith(1);
});