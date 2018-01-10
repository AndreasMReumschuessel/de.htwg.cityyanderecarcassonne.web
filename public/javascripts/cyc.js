// Carcassonne Frontend Logic

// Add a new player
$('#addplayerbtn').click(function (ev) {
    var name = $('#playername').val();
    console.log("Trying to add player: " + name);

    var player = createPlayerObject(name);
    $('.playerpanel').append(player);
});

function createPlayerObject(name) {
    return $(document.createElement('div'))
            .addClass('player')
            .attr("id", name);
}