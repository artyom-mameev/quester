function changeToFavoriteState(gameId) {
    $('#favorite-button-' + gameId).removeClass('far fa-star')
        .addClass('fas fa-star').off().click(function () {
        unfavorite(gameId);
    });
}

function changeToUnfavoriteState(gameId) {
    $('#favorite-button-' + gameId).removeClass('fas fa-star')
        .addClass('far fa-star').off().click(function () {
        favorite(gameId);
    });
}

try {
    module.exports = {
        changeToFavoriteState, changeToUnfavoriteState
    }
} catch (e) {
    if (e instanceof ReferenceError) {
        /*
        There is no need to do something, module exports are only needed in
        nodejs unit tests.
        */
    } else throw e;
}