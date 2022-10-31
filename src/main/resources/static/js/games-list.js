function setUpSortingSelect() {
    const href = new URL(location.href);

    const sort = href.searchParams.get('sort');

    $("#sortingSelect").val(sort).change(function () {
        const sortMode = $("#sortingSelect option:selected").val();

        changeSort(sortMode);
    });
}

function changePage(page) {
    const href = new URL(location.href);

    href.searchParams.set('page', page);

    location.assign(href.toString());
}

function changeSort(sort) {
    const href = new URL(location.href);

    href.searchParams.set('sort', sort);

    location.assign(href.toString());
}

try {
    module.exports = {
        setUpSortingSelect, changePage
    };
} catch (e) {
    if (e instanceof ReferenceError) {
        /*
        There is no need to do something, module exports are only needed in
        nodejs unit tests.
        */
    } else throw e;
}