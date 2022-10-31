function setUpCreatingComments() {
    $('#send-comment-button').off().click(onCreateCommentClick);
}

function onCreateCommentClick() {
    clearSendCommentConstraintErrors();

    const gameId = $('#send-comment-button').data('game-id');
    const text = $('textarea[name="comment"]').val().trim();

    makeAjaxRequest(HTTP_REQUEST_TYPE.POST,
        "/api/games/" + gameId + "/comments", {text: text},
        sendCommentCallback);
}

function sendCommentCallback(data) {
    if (data.responseJSON.hasErrors) {
        for (let error of data.responseJSON.fieldErrors) {
            addSendCommentConstraintError(error.defaultMessage);
        }
    } else {
        location.reload();
    }
}

function setUpEditingComments() {
    $('#editCommentModal').off().on('show.bs.modal', onEditCommentClick);
}

function onEditCommentClick(event) {
    const button = $(event.relatedTarget);
    const commentId = button.data('comment-id');
    const gameId = button.data('game-id');
    const text = button.data('text');

    $('#comment-text').val(text);

    $('#modal-button-save').off().click(
        onEditCommentSaveButtonClick(text, gameId, commentId));
}

function onEditCommentSaveButtonClick(text, gameId, commentId) {
    return function () {
        const editedText = $('#comment-text').val().trim();

        if (editedText === text) { // if the edited text is the same
            return;
        }

        clearEditCommentConstraintErrors();

        makeAjaxRequest(HTTP_REQUEST_TYPE.PUT,
            "/api/games/" + gameId + "/comments/" + commentId,
            {text: editedText}, editCommentCallback);
    }
}

function editCommentCallback(response) {
    if (response.responseJSON.hasErrors) {
        for (let error of response.responseJSON.fieldErrors) {
            addEditCommentConstraintError(error.defaultMessage);
        }
    } else {
        location.reload();
    }
}

function deleteComment(commentId, gameId) {
    showAlertModal(DELETE_COMMENT_STRING, ARE_YOU_SURE_STRING,
        onConfirmingToDeleteComment(gameId, commentId), ALERT_MODE.QUESTION);
}

function onConfirmingToDeleteComment(gameId, commentId) {
    return function () {
        makeAjaxRequest(HTTP_REQUEST_TYPE.DELETE,
            "/api/games/" + gameId + "/comments/" + commentId, null,
            deleteCommentCallback);
    };
}

function deleteCommentCallback(data) {
    if (data.responseJSON === true) {
        location.reload();
    } else {
        showSimpleErrorModal(CANNOT_DELETE_THE_COMMENT_STRING);
    }
}

function addSendCommentConstraintError(newError) {
    addCommentConstraintError("comment-error-div",
        "comment-error", newError);
}

function clearSendCommentConstraintErrors() {
    clearCommentConstraintErrors("comment-error-div",
        "comment-error");
}

function addEditCommentConstraintError(newError) {
    addCommentConstraintError("comment-error-div-modal",
        "comment-error-modal", newError);
}

function clearEditCommentConstraintErrors() {
    clearCommentConstraintErrors("comment-error-div-modal",
        "comment-error-modal");
}

function addCommentConstraintError(errorDivId, errorLabelId, newError) {
    $('#' + errorDivId).removeAttr("hidden");

    const errorText = $(errorLabelId).html();

    $('#' + errorLabelId)
        .append(errorText === "" ? "" + newError : "<br>" + newError);
}

function clearCommentConstraintErrors(errorDivId, errorLabelId) {
    $('#' + errorDivId).attr('hidden', 'true');
    $('#' + errorLabelId).html("");
}

try {
    module.exports = {
        setUpCreatingComments, setUpEditingComments, deleteComment
    };
} catch (e) {
    if (e instanceof ReferenceError) {
        /*
        There is no need to do something, module exports are only needed in
        nodejs unit tests.
        */
    } else throw e;
}