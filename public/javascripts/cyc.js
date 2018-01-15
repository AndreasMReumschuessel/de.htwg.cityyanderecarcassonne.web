// Carcassonne Frontend Logic

// Add a new player
$('#addplayerbtn').click(function (ev) {
    var name = $('#playername').val();
    console.log("Trying to add player: " + name);

    var player = createPlayerObject(name);
    $('.playerlist').append(player);

    $.ajax({
        url: "/cyc/addplayer/" + name,
        type: "GET",
        dataType: "json",
        success: function(result){
            player.prepend(result.meeple + " : " + result.name + " : " + result.score);
            $('#playername').prop("value", "");
            console.log("AJAX succeeded! Player name: " + result.meeple);
        }
    })
});

function createPlayerObject(name) {
    return $(document.createElement('div'))
            .addClass('player')
            .attr("id", name);
}