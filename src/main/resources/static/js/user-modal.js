function setUpUserModal() {
    $('#userModal').off().on('show.bs.modal', function (event) {
        const eventButton = $(event.relatedTarget);

        const userName = eventButton.data('user-name');
        const userId = eventButton.data('user-id');
        const userEnabled = eventButton.data('user-enabled');
        const userRating = eventButton.data('user-rating');
        const userGamesCount = eventButton.data('user-games-count');
        const userRatedCount = eventButton.data('user-rated-count');
        const userCommentsCount = eventButton.data('user-comments-count');

        $('#user-modal-title').text(userName);
        $('#rating-label').text(userRating + "/5.0");

        $('#games-count-label').text(userGamesCount);
        $('#rated-count-label').text(userRatedCount);
        $('#comments-count-label').text(userCommentsCount);

        if (userEnabled) {
            $('#banButton')
                .attr("class", "fas fa-ban") // change the mode to ban button
                .click(function () {
                    ban(userId);
                });

            $('#banned').hide(); // hide 'banned' label
        } else {
            $('#banButton')
                .attr("class", "fas fa-undo") // change the mode to unban button
                .click(function () {
                    unban(userId);
                });
        }

        if (userGamesCount > 0) {
            $('#show-all-games-link').attr("href", "/games?user=" + userId);

            $('#delete-all-games-button').off().click(function () {
                deleteAllGames(userId);
            });
        } else {
            $('#show-all-games-link').hide();
            $('#delete-all-games-button').hide();
        }

        if (userCommentsCount > 0) {
            $('#show-all-comments-link').attr("href", "/comments?user=" + userId);

            $('#delete-all-comments-button').off().click(function () {
                deleteAllComments(userId);
            });
        } else {
            $('#show-all-comments-link').hide();
            $('#delete-all-comments-button').hide();
        }

        if (userRatedCount > 0) {
            $('#delete-all-reviews-button').off().click(function () {
                deleteAllReviews(userId);
            });
        } else {
            $('#delete-all-reviews-button').hide();
        }
    });
}

function ban(userId) {
    showAlertModal(BAN_USER_STRING, ARE_YOU_SURE_STRING,
        onConfirmingToBan(userId), ALERT_MODE.QUESTION);
}

function onConfirmingToBan(userId) {
    return function () {
        makeAjaxRequest(HTTP_REQUEST_TYPE.POST,
            "/api/users/" + userId + "/ban", null, banCallback);
    };
}

function banCallback(response) {
    if (response.responseJSON === true) {
        location.reload();
    } else {
        showSimpleErrorModal(CAN_NOT_BAN_STRING);
    }
}

function unban(userId) {
    showAlertModal(UNBAN_USER_STRING, ARE_YOU_SURE_STRING,
        onConfirmingToUnban(userId), ALERT_MODE.QUESTION);
}

function onConfirmingToUnban(userId) {
    return function () {
        makeAjaxRequest(HTTP_REQUEST_TYPE.DELETE,
            "/api/users/" + userId + "/ban", null, unbanCallback);
    };
}

function unbanCallback(response) {
    if (response.responseJSON === true) {
        location.reload();
    } else {
        showSimpleErrorModal(CAN_NOT_UNBAN_STRING);
    }
}

function deleteAllGames(userId) {
    showAlertModal(DELETE_ALL_GAMES_STRING, ARE_YOU_SURE_STRING,
        onConfirmingToDeleteAllGames(userId), ALERT_MODE.QUESTION);
}

function onConfirmingToDeleteAllGames(userId) {
    return function () {
        makeAjaxRequest(HTTP_REQUEST_TYPE.DELETE,
            "/api/users/" + userId + "/games", null,
            deleteAllGamesCallback);
    };
}

function deleteAllGamesCallback(response) {
    if (response.responseJSON === true) {
        location.replace(location.origin);
    } else {
        showSimpleErrorModal(CAN_NOT_DELETE_ALL_GAMES_STRING);
    }
}

function deleteAllComments(userId) {
    showAlertModal(DELETE_ALL_COMMENTS_STRING, ARE_YOU_SURE_STRING,
        onConfirmingToDeleteAllComments(userId), ALERT_MODE.QUESTION);
}

function onConfirmingToDeleteAllComments(userId) {
    return function () {
        makeAjaxRequest(HTTP_REQUEST_TYPE.DELETE,
            "/api/users/" + userId + "/comments", null,
            deleteAllCommentsCallback);
    };
}

function deleteAllCommentsCallback(response) {
    if (response.responseJSON === true) {
        location.reload();
    } else {
        showSimpleErrorModal(CAN_NOT_DELETE_ALL_COMMENTS_STRING);
    }
}

function deleteAllReviews(userId) {
    showAlertModal(DELETE_ALL_RATED_STRING, ARE_YOU_SURE_STRING,
        onConfirmingToDeleteAllReviews(userId), ALERT_MODE.QUESTION);
}

function onConfirmingToDeleteAllReviews(userId) {
    return function () {
        makeAjaxRequest(HTTP_REQUEST_TYPE.DELETE,
            "/api/users/" + userId + "/reviews", null,
            deleteAllReviewsCallback);
    };
}

function deleteAllReviewsCallback(response) {
    if (response.responseJSON === true) {
        location.reload();
    } else {
        showSimpleErrorModal(CAN_NOT_DELETE_ALL_RATED_STRING);
    }
}

try {
    module.exports = {
        setUpUserModal
    };
} catch (e) {
    if (e instanceof ReferenceError) {
        /*
        There is no need to do something, module exports are only needed in
        nodejs unit tests.
        */
    } else throw e;
}