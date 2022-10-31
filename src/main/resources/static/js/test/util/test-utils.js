function getVar(variable) {
    return global[variable];
}

function mockVar(variable, mock) {
    global[variable] = mock;
    return global[variable];
}

function mockFunc(func) {
    global[func] = jest.fn();
    return global[func];
}

function mockFuncs(...funcs) {
    for (const func of funcs) {
        mockFunc(func);
    }
}

function mockImpl(func, impl) {
    global[func] = jest.fn().mockImplementation(impl);
    return global[func];
}

function mockLocation() {
    Object.defineProperty(window, 'location', {
        configurable: true,
        value: {
            reload: jest.fn(),
            replace: jest.fn(),
            assign: jest.fn(),
            href: {},
            origin: "testOrigin"
        },
    });
    return window.location;
}

function hasAttr(id, attr) {
    const attribute = $("#" + id).attr(attr);
    return (typeof attribute !== 'undefined' &&
        attribute !== false);
}

function hasEvent(id, eventName, count) {
    const eventListeners = $._data($('#' + id)[0], "events");
    return eventListeners[eventName].length === count;
}

function hasEventWithNamespace(id, eventName, index, namespace) {
    const eventListeners = $._data($('#' + id)[0], "events");
    return eventListeners[eventName][index].namespace === namespace;
}

function setSimpleEventHandler(id, eventName) {
    $('#' + id).off().on(eventName, function (event) {
        console.log(event + ' does nothing');
    });
}

function trueRequestResult(type, url, data, func) {
    data = {
        responseJSON: true
    }
    func(data);
}

function falseRequestResult(type, url, data, func) {
    data = {
        responseJSON: false
    }
    func(data);
}

function requestResultWithoutFieldError(type, url, data, func) {
    data = {
        responseJSON: {
            hasErrors: false
        }
    }
    func(data);
}

function requestResultWithFieldError(type, url, data, func) {
    data = {
        responseJSON: {
            hasErrors: true,
            fieldErrors: [{defaultMessage: "TestError"}]
        }
    }
    func(data);
}

function mockAlertModalThatJustRunsFunc(title, desc, func) {
    if (func != null) {
        func();
    }
}

module.exports = {
    getVar,
    mockVar,
    mockFunc,
    mockFuncs,
    mockImpl,
    mockLocation,
    hasAttr,
    hasEvent,
    hasEventWithNamespace,
    setSimpleEventHandler,
    trueRequestResult,
    falseRequestResult,
    requestResultWithoutFieldError,
    requestResultWithFieldError,
    mockAlertModalThatJustRunsFunc
}