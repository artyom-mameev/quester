function setUpPagination(currentPage, totalPages) {
    $('#luckmoshy').luckmoshyPagination({
        totalPages: totalPages,
        startPage: currentPage,
        visiblePages: 5,
        initiateStartPageClick: false,
        loop: false,
        first: 'First',
        prev: "<<",
        next: ">>",
        last: 'Last',

        onPageClick: function (event, page) {
            let href = new URL(window.location.href);

            href.searchParams.set('page', page);

            window.location.assign(href);
        }
    });
}