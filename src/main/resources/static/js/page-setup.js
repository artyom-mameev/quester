function setTitle(title) {
    $(document).attr("title", title);
}

function setActiveMenu(activeMenu) {
    $('#' + activeMenu)
        .attr("class", "nav-link active btn-group")
        .attr("aria-current", "page");

    if (activeMenu === "user-navbar") {
        $('#user-navbar').addClass("profile-link")
    }
}

function logout() {
    makeAjaxRequest(HTTP_REQUEST_TYPE.POST, "/logout", null,
        logoutCallback);
}

function logoutCallback() {
    window.location.reload(window.location.origin);
}

try {
    module.exports = {
        setTitle, setActiveMenu, logout
    };
} catch (e) {
    if (e instanceof ReferenceError) {
        /*
        There is no need to do something, module exports are only needed in
        nodejs unit tests.
        */
    } else throw e;
}
