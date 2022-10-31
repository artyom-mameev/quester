/**
 * @jest-environment jsdom
 */

const commonsModule = require("../commons");
const testUtils = require("./util/test-utils.js")

beforeAll(() => {
    setUpJquery();
    setUpGlobalVariables();
    setUpMocks();
    setUpHtml();
});

function setUpJquery() {
    global.$ = require("jquery");
    global.jQuery = global.$;
}

function setUpGlobalVariables() {
    global.YES_STRING = "YES";
    global.ERROR_STRING = "ERROR";
    global.ERROR_IS_OCCURRED_STRING = "ERROR_IS_OCCURED";
    global.ALERT_MODE = {
        INFO: "INFO",
        QUESTION: "QUESTION"
    };
    global.HTTP_REQUEST_TYPE = {
        POST: "POST",
        PUT: "PUT",
        DELETE: "DELETE",
    }
}

function setUpMocks() {
    jest.spyOn($.fn, 'show');
    jest.spyOn($.fn, 'hide');
    jest.spyOn($.fn, 'off');
    jest.spyOn($.fn, 'on');
    jest.spyOn($.fn, 'click');

    global.$.fn.modal = jest.fn();
}

function setUpHtml() {
    document.body.innerHTML =
        '<div id="alertModal" class="modal" tabindex="-1" role="dialog">\n' +
        '        <div class="modal-dialog modal-sm modal-dialog-centered" role="document">\n' +
        '            <div class="modal-content">\n' +
        '                <div class="modal-header">\n' +
        '                    <h5 id="alertModalTitle" class="modal-title"></h5>\n' +
        '                    <button aria-label="Close" type="button" class="close" data-dismiss="modal">\n' +
        '                        <span aria-hidden="true">&times;</span>\n' +
        '                    </button>\n' +
        '                </div>\n' +
        '                <div class="modal-body">\n' +
        '                    <p id="alertModalBodyText" class="no-bottom-margin"></p>\n' +
        '                </div>\n' +
        '                <div class="modal-footer">\n' +
        '                    <button id="alertModalYesButton" th:text="#{yes}" type="button" class="btn btn-light" data-dismiss="modal"></button>\n' +
        '                    <button id="alertModalNoButton" th:text="#{no}" type="button" class="btn btn-secondary" data-dismiss="modal"></button>\n' +
        '                </div>\n' +
        '            </div>\n' +
        '        </div>\n' +
        '    </div>\n';
}

test('showAlertModal sets modal text', () => {
    commonsModule.showAlertModal("test-title", "test-text",
        Function, ALERT_MODE.QUESTION);

    expect($('#alertModalTitle').text()).toEqual('test-title');
    expect($('#alertModalBodyText').text()).toEqual('test-text');
});

test('showAlertModal shows modal', () => {
    commonsModule.showAlertModal("test-title", "test-text",
        Function, ALERT_MODE.QUESTION);

    expect($().modal).toHaveBeenCalledTimes(1);
});

test('showAlertModal hides "no button" and changes confirm button into ' +
    '"ok button" if mode is INFO', () => {
    commonsModule.showAlertModal("test-title", "test-text",
        function () {
        }, ALERT_MODE.INFO);

    $("#alertModal").trigger('show.bs.modal');

    expect($('#alertModalNoButton').css("display") === 'none')
        .toBe(true);
    expect($('#alertModalYesButton').html()).toBe("OK");
});

test('showAlertModal shows "no button" and changes "confirm button" into ' +
    '"yes button" if mode is QUESTION', () => {
    commonsModule.showAlertModal("test-title", "test-text",
        function () {
        }, ALERT_MODE.QUESTION);
    $("#alertModal").trigger('show.bs.modal');

    expect($('#alertModalNoButton').css("display") !== 'none')
        .toBe(true);
    expect($('#alertModalYesButton').html()).toBe(YES_STRING);
});

test('showAlertModal adds on click listener to "yes button"', () => {
    commonsModule.showAlertModal("test-title", "test-text",
        function () {
        }, ALERT_MODE.QUESTION);

    expect(testUtils.hasEvent("alertModalYesButton", "click",
        1)).toBe(true);
});

test('showAlertModal calls function when "yes button" is clicked', () => {
    let func = jest.fn();

    commonsModule.showAlertModal("test-title", "test-text",
        func, ALERT_MODE.QUESTION);

    $("#alertModalYesButton").trigger('click');

    expect(func).toHaveBeenCalledTimes(1);
});

test('showAlertModal throws UnknownAlertModeException if mode is unknown', () => {
    const showAlertModalWithError = () => {
        commonsModule.showAlertModal("test-title", "test-text",
            Function, "UNKNOWN_MODE")
    }

    expect(showAlertModalWithError)
        .toThrow(commonsModule.UnknownAlertModeException);
});

test('showSimpleErrorModal sets modal text', () => {
    commonsModule.showSimpleErrorModal("test-title");

    expect($('#alertModalTitle').text()).toEqual(ERROR_STRING);
    expect($('#alertModalBodyText').text()).toEqual("test-title");
    expect($("#alertModalYesButton").html()).toEqual("OK");
});

test('showSimpleErrorModal calls modal', () => {
    commonsModule.showSimpleErrorModal("test-title");

    expect($().modal).toHaveBeenCalledTimes(1);
});

test('showSimpleErrorModal without arguments shows default error message', () => {
    commonsModule.showSimpleErrorModal();

    expect($('#alertModalBodyText').text()).toEqual(ERROR_IS_OCCURRED_STRING);
});

test('makeAjaxRequest sends POST request', () => {
    mockJqueryAjax();

    function func() {
    }

    commonsModule.makeAjaxRequest(HTTP_REQUEST_TYPE.POST, "test.url",
        "test.data", func);

    expect($.ajax).toHaveBeenCalledTimes(1);
    expect($.ajax).toHaveBeenCalledWith({
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
        },
        'type': 'POST',
        'url': "test.url",
        'data': JSON.stringify("test.data"),
        'dataType': 'json',
        'complete': func,
    });
});

test('makeAjaxRequest sends PUT request', () => {
    mockJqueryAjax();

    function func() {
    }

    commonsModule.makeAjaxRequest(HTTP_REQUEST_TYPE.PUT, "test.url",
        "test.data", func);

    expect($.ajax).toHaveBeenCalledTimes(1);
    expect($.ajax).toHaveBeenCalledWith({
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
        },
        'type': 'PUT',
        'url': "test.url",
        'data': JSON.stringify("test.data"),
        'dataType': 'json',
        'complete': func,
    });
});

test('makeAjaxRequest sends DELETE request', () => {
    mockJqueryAjax();

    function func() {
    }

    commonsModule.makeAjaxRequest(HTTP_REQUEST_TYPE.DELETE, "test.url",
        "test.data", func);

    expect($.ajax).toHaveBeenCalledTimes(1);
    expect($.ajax).toHaveBeenCalledWith({
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
        },
        'type': 'DELETE',
        'url': "test.url",
        'data': JSON.stringify("test.data"),
        'dataType': 'json',
        'complete': func,
    });
});

function mockJqueryAjax() {
    global.$.ajax = jest.fn().mockImplementation(() => {
        const fakeResponse = {
            id: 1,
            name: "All",
            value: "Dummy Data"
        };
        return Promise.resolve(fakeResponse);
    });
}



