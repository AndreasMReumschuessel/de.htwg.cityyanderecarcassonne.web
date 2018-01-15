// Carcassonne Frontend Logic

// Add a new player
$('#addplayerbtn').click(function (ev) {
    var name = $('#playername').val();
    console.log("Trying to add player: " + name);

    var player = createPlayerObject(name);
    $('.playerpanel').append(player);

    $.ajax({
        url: "@routes.CarcassonneWebController.json",
        type: "POST",
        dataType: "text",
        data: "p" + name,
        success: function(result){
            console.log("AJAX succeeded! Player name: " + result);
        }
    })
});

function createPlayerObject(name) {
    return $(document.createElement('div'))
            .addClass('player')
            .attr("id", name);
}