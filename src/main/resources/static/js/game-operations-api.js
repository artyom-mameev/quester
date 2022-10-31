function favorite(gameId) {
    makeAjaxRequest(HTTP_REQUEST_TYPE.POST,
        "/api/games/" + gameId + "/favorites", null,
        favoriteCallback(gameId));
}

function favoriteCallback(gameId) {
    return function (response) {
        if (response.responseJSON === true) {
            changeToFavoriteState(gameId);
        } else {
            showSimpleErrorModal(CANNOT_MAKE_A_FAVORITE_STRING);
        }
    }
}

function unfavorite(gameId) {
    makeAjaxRequest(HTTP_REQUEST_TYPE.DELETE,
        "/api/games/" + gameId + "/favorites", null,
        unfavoriteCallback(gameId));
}

function unfavoriteCallback(gameId) {
    return function (response) {
        if (response.responseJSON === true) {
            changeToUnfavoriteState(gameId);
        } else {
            showSimpleErrorModal(CANNOT_UNFAVORITE_STRING);
        }
    };
}

function deleteGame(id) {
    showAlertModal(DELETE_GAME_STRING, ARE_YOU_SURE_STRING,
        onConfirmingToDeleteGame(id), ALERT_MODE.QUESTION);
}

function onConfirmingToDeleteGame(id) {
    return function () {
        makeAjaxRequest(HTTP_REQUEST_TYPE.DELETE, "/api/games/" + id,
            null, deleteGameCallback);
    };
}

function deleteGameCallback(response) {
    if (response.responseJSON === true) {
        let href = location.origin;

        href += "/games";

        location.href = href;
    } else {
        showSimpleErrorModal(CANNOT_DELETE_THE_GAME_STRING);
    }
}

try {
    module.exports = {
        favorite, unfavorite, deleteGame
    }
} catch (e) {
    if (e instanceof ReferenceError) {
        /*
        There is no need to do something, module exports are only needed in
        nodejs unit tests.
        */
    } else throw e;
}