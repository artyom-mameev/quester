function showAlertModal(title, text, func, mode) {
    const alertModalYesButton = $("#alertModalYesButton");
    const alertModalNoButton = $("#alertModalNoButton");

    switch (mode) {
        case ALERT_MODE.INFO:
            alertModalYesButton.html("OK").off().click(func);
            alertModalNoButton.hide();
            break;

        case ALERT_MODE.QUESTION:
            alertModalYesButton.html(YES_STRING).off().click(func);
            alertModalNoButton.show();
            break;

        default:
            throw new UnknownAlertModeException("Unknown alert mode");
    }

    $("#alertModalTitle").text(title);
    $("#alertModalBodyText").text(text);
    $("#alertModal").modal();
}

function showSimpleErrorModal(string = ERROR_IS_OCCURRED_STRING) {
    showAlertModal(ERROR_STRING, string, null, ALERT_MODE.INFO);
}

function makeAjaxRequest(type, url, data, func) {
    jQuery.ajax({
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
        },
        'type': type,
        'url': url,
        'data': JSON.stringify(data),
        'dataType': 'json',
        'complete': func,
    });
}

function UnknownAlertModeException(message) {
    this.message = message;
    this.name = "Unknown Alert Mode Exception";
}

try {
    module.exports = {
        showAlertModal, showSimpleErrorModal, makeAjaxRequest,
        UnknownAlertModeException
    };
} catch (e) {
    if (e instanceof ReferenceError) {
        /*
        There is no need to do something, module exports are only needed in
        nodejs unit tests.
        */
    } else throw e;
}
