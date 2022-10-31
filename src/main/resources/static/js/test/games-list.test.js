/**
 * @jest-environment jsdom
 */

const gamesListModule = require("../games-list");
const testUtils = require("./util/test-utils.js")

beforeAll(() => {
    setUpJquery();
});

beforeEach(() => {
    setUpHtml();
});

function setUpJquery() {
    global.$ = require("jquery");
    global.jQuery = global.$;
}

function setUpHtml() {
    document.body.innerHTML =
        "        <!--suppress HtmlUnknownAttribute --><select id=\"sortingSelect\" class=\"form-select form-select-sm\"\n" +
        "                style=\"width:8em\" totalPages=\"1\">\n" +
        "            <option disabled th:text=\"#{games.sortBy}\"></option>\n" +
        "            <option th:text=\"#{games.newest}\" value=\"newest\"></option>\n" +
        "            <option selected th:text=\"#{games.oldest}\" value=\"oldest\"></option>\n" +
        "            <option th:text=\"#{games.rating}\" value=\"rating\"></option>\n" +
        "        </select></p>\n" +
        "            <div class=\"col\" th:each=\"game: ${games}\">\n" +
        "                <div class=\"card\" style=\"width: 18rem;\" >\n" +
        "                    <div class=\"card-body\">\n" +
        "                        <h5 class=\"card-title\"><a href=\"#\" data-game-id=\"TestGameId\" th:text=\"${game.name}\" class=\"game-name\"></a>\n" +
        "                            <span th:text=\"${game.language}\" class=\"badge bg-secondary\"></span>\n" +
        "                            <a sec:authorize=\"isAuthenticated()\"\n" +
        "                               th:attr=\"data-game-id=${game.id}\"\n" +
        "                               class=\"far fa-star\" href=\"#\"></a>\n" +
        "                            <a data-game-id=\"TestGameId\" class=\"fas fa-edit\" id=\"game-edit\" href=\"#\"></a>\n" +
        "                            <a sec:authorize=\"isAuthenticated()\" th:if=\"${game.userId == userId or isAdmin == true}\"\n" +
        "                               th:attr=\"onclick=|deleteGame('${game.id}')|\" class=\"far fa-trash-alt\" href=\"#\"></a>\n" +
        "                        </h5>\n" +
        "                        <p class=\"card-text\"><label th:text=\"${game.description}\" class=\"card-text\"></label></p>\n" +
        "                        <div class=\"rating\"\n" +
        "                             th:attr=\"data-game-id=${game.id}\"></div>\n" +
        "                        <p class=\"card-text float-right\"><a data-game-id=\"TestGameId\" id=\"comments-count\" class=\"comments-count\" href=\"#\"></a><br>\n" +
        "                            <small th:text=\"${#dates.format(game.date, 'dd.MM.yyyy')}\"\n" +
        "                                   class=\"text-muted float-right\"></small></p>\n" +
        "                        <h5 class=\"card-title\"><a href=\"#\" data-game-id=\"TestGameId2\" th:text=\"${game.name}\" class=\"game-name\"></a>\n" +
        "                            <span th:text=\"${game.language}\" class=\"badge bg-secondary\"></span>\n" +
        "                            <a sec:authorize=\"isAuthenticated()\"\n" +
        "                               th:attr=\"data-game-id=${game.id}\"\n" +
        "                               class=\"far fa-star\" href=\"#\"></a>\n" +
        "                            <a data-game-id=\"TestGameId2\" class=\"fas fa-edit\" id=\"game-edit\" href=\"#\"></a>\n" +
        "                            <a sec:authorize=\"isAuthenticated()\" th:if=\"${game.userId == userId or isAdmin == true}\"\n" +
        "                               th:attr=\"onclick=|deleteGame('${game.id}')|\" class=\"far fa-trash-alt\" href=\"#\"></a>\n" +
        "                        </h5>\n" +
        "                        <p class=\"card-text\"><label th:text=\"${game.description}\" class=\"card-text\"></label></p>\n" +
        "                        <div class=\"rating\"\n" +
        "                             th:attr=\"data-game-id=${game.id}\"></div>\n" +
        "                        <p class=\"card-text float-right\"><a data-game-id=\"TestGameId2\" id=\"comments-count\" class=\"comments-count\" href=\"#\"></a><br>\n" +
        "                            <small th:text=\"${#dates.format(game.date, 'dd.MM.yyyy')}\"\n" +
        "                                   class=\"text-muted float-right\"></small></p>\n" +
        "                    </div>\n" +
        "                </div>\n" +
        "            </div>\n";
}

test('setUpSortingSelect changes sort method select depending on url', () => {
    const locationMock = testUtils.mockLocation();
    locationMock.href = "http://test.com/games?sort=newest";

    gamesListModule.setUpSortingSelect();

    const sortingSelect = $("#sortingSelect");

    expect(sortingSelect.val()).toEqual("newest");

    locationMock.href = "http://test.com/games?sort=oldest";

    gamesListModule.setUpSortingSelect();

    expect(sortingSelect.val()).toEqual("oldest");

    locationMock.href = "http://test.com/games?sort=rating";

    gamesListModule.setUpSortingSelect();

    expect(sortingSelect.val()).toEqual("rating");
});

test('setUpSortingSelect sets that changing sort select choice should make' +
    ' redirect to games page with selected sort parameter as url parameter', () => {
    const locationMock = testUtils.mockLocation();
    locationMock.href = "http://test.com/games?sort=newest";

    gamesListModule.setUpSortingSelect();

    const sortingSelect = $("#sortingSelect");

    sortingSelect.val("oldest").trigger("change");

    expect(locationMock.assign).toHaveBeenCalledTimes(1);
    expect(locationMock.assign).toHaveBeenCalledWith(
        "http://test.com/games?sort=oldest");

    sortingSelect.val("newest").trigger("change");

    expect(locationMock.assign).toHaveBeenCalledTimes(2);
    expect(locationMock.assign).toHaveBeenCalledWith(
        "http://test.com/games?sort=newest");

    sortingSelect.val("rating").trigger("change");

    expect(locationMock.assign).toHaveBeenCalledTimes(3);
    expect(locationMock.assign).toHaveBeenCalledWith(
        "http://test.com/games?sort=rating");
});

test('changePage redirects to specific games page with page number ' +
    ' as url parameter', () => {
    const locationMock = testUtils.mockLocation();
    locationMock.href = "http://test.com/games?page=1";

    gamesListModule.changePage(2);

    expect(locationMock.assign).toHaveBeenCalledTimes(1);
    expect(locationMock.assign).toHaveBeenCalledWith(
        "http://test.com/games?page=2");
});