function changeRoomTitle(title) {
    $("#room-name").text(title);
}

function changeRoomDesc(desc) {
    $("#room-description").html(desc);
}

function createGameChoice(choice, activeFlags) {
    createChoice(choice.text, choice.id, function () {
        if (choice.type === NODE_TYPE.FLAG) {
            activeFlags.push(choice.id); // trigger the flag
        }

        goToNextRoom(choice.nextRoom);
    });
}

function createGameOverChoices() {
    createChoice(START_AGAIN_STRING, "start-again", function () {
        startAgain();
    });

    createChoice(RETURN_TO_GAME_PAGE_STRING, "return", function () {
        returnToGamePage();
    });
}

function clearChoices() {
    $('#choices').empty();
}

function createChoice(text, id, func) {
    $('#choices').append('<a href="#" id="' + id + '">' + text + '</a><br>');

    $('a#' + id).on('click', func);
}

function startAgain() {
    location.reload();
}

function returnToGamePage() {
    const gameId = $('#game').data('game-id');

    let href = location.origin;

    href = href + "/games/" + gameId;

    location.assign(href);
}

try {
    module.exports = {
        changeRoomTitle, changeRoomDesc, createGameChoice,
        createGameOverChoices, clearChoices
    };
} catch (e) {
    if (e instanceof ReferenceError) {
        /*
        There is no need to do something, module exports are only needed in
        nodejs unit tests.
        */
    } else throw e;
}
