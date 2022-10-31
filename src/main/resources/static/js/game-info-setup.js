function setUpRateModal(gameId, userReview) {
    $('#ratingModal').off().on('show.bs.modal',
        onRatingModalShow(userReview, gameId));
}

function onRatingModalShow(userReview, gameId) {
    return function () {
        $('.review').rating({
                "color": "#FFF",
                "value": userReview,
                "click": onReviewClick(userReview, gameId)
            }
        );
    }
}

function onReviewClick(userReview, gameId) {
    return function (e) {
        if (e.stars === userReview) {
            return;
        }

        const requestType = userReview === 0 ? HTTP_REQUEST_TYPE.POST :
            HTTP_REQUEST_TYPE.PUT;

        makeAjaxRequest(requestType, "/api/games/" + gameId + "/rate",
            {rating: e.stars}, sendReviewCallback);

    };
}

function sendReviewCallback(response) {
    if (response.responseJSON === true) {
        location.reload();
    } else {
        showSimpleErrorModal(CANNOT_SEND_THE_REVIEW_STRING);
    }
}

try {
    module.exports = {
        setUpRateModal
    };
} catch (e) {
    if (e instanceof ReferenceError) {
        /*
        There is no need to do something, module exports are only needed in
        nodejs unit tests.
        */
    } else throw e;
}