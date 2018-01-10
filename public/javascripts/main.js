$.getScript("/assets/javascripts/cyc.js", function() {
});

$(document).ready(function() {
    $('button#roundctrl').click(function(ev) {
        console.log('clicked', $( this ).text());
        if ($('div.game').hasClass("hallo")) {
            $('div.game').removeClass("hallo");
        } else {
            $('div.game').addClass("hallo");
        }
        $('#6_8').addClass("active");
    });

    // Every time a modal is shown, if it has an autofocus element, focus on it.
    $('.modal').on('shown.bs.modal', function() {
        $(this).find('[autofocus]').focus();
    });
});