// Carcassonne Frontend Logic

// Get game status and do logic
gamestatus = getGameStatus()

if (gamestatus === "WELCOME" || gamestatus === "PLAYER_ADDED") {
    $('.currentcard').hide()
    $('.cardsleft').hide()
    disable($('#rotleft'))
    disable($('#rotright'))
    disable($('#roundctrl'))

    checkGameStartable()
}

if (gamestatus === "ROUND_START") {
    roundStarted()
} else if (gamestatus === "CARD_ROTATED") {
    showCurrentCard()
}

// Add a new player
$('#addplayerbtn').click(function (ev) {
    var name = $('#playername').val()
    console.debug("Trying to add player: " + name)

    $.ajax({
        url: "/cyc/addplayer/" + name,
        type: "GET",
        dataType: "json",
        success: function(result){
            var player = createPlayerObject(name)
            player.prepend(result.meeple + " : " + result.name + " : " + result.score)
            $('.playerlist').append(player)

            $('#playername').prop("value", "")
            console.debug("AJAX succeeded! Player name: " + result.name)

            checkGameStartable()
        },
        error: function(jqxhr, errstatus, errmsg) {
            console.error("Addplayer function: " + errstatus + " -> " + errmsg)
        }
    })
});

function createPlayerObject(name) {
    return $(document.createElement('div'))
            .addClass('player')
            .attr("id", name);
}

// Start game if possible
$('#roundctrl').click(function(ev) {
    gamestatus = getGameStatus()

    if (gamestatus === "PLAYER_ADDED") {
        $.ajax({
            url: "/cyc/creategame/",
            type: "GET",
            dataType: "text",
            success: function (result) {
                roundStarted()
            },
            error: function (jqxhr, errstatus, errmsg) {
                console.error("Creategame function: " + errstatus + " -> " + errmsg)
            }
        })
    }
});

// Status functions
function checkGameStartable() {
    if ($('.player').length > 0) {
        enable($('#roundctrl'))
    }
}

function roundStarted() {
    console.info("A new round has started!")
    $('#addplayer').hide()
    $('.currentcard').show()
    $('.cardsleft').show()
    enable($('#rotleft'))
    enable($('#rotright'))

    $('#roundctrl').html("Finish Round")
    disable($('#roundctrl'))

    showActivePlayer()
    showCurrentCard()
    showRemainingCards()
    //showCardPossibilities()
}

function showActivePlayer() {
    $.ajax({
        url: "/cyc/currentplayer/",
        type: "GET",
        dataType: "text",
        success: function(result) {
            console.debug("Currentplayer: " + result)
            $('#' + result).addClass("active")
        },
        error: function(jqxhr, errstatus, errmsg) {
            console.error("showActivePlayer function: " + errstatus + " -> " + errmsg)
        }
    })
}

function showCurrentCard() {
    $.ajax({
        url: "/cyc/currentcard/",
        type: "GET",
        dataType: "json",
        success: function (currCard) {
            console.debug("Current card: " + currCard.cardname)

            if ($('.currentcard > img').length === 0) {
                cardimage = $(document.createElement('img'))
                    .addClass('img-responsive')
                    .attr("src", "/assets/cyc-data/" + currCard.cardname + ".png")
                $('.currentcard').append(cardimage)
            }
            rotateCard(".currentcard", currCard.orientation)
        },
        error: function (jqxhr, errstatus, errmsg) {
            console.error("showCurrentCard function: " + errstatus + " -> " + errmsg)
        }
    })
}

function rotateCard(divid, orientation) {
    $(divid + ' > img').rotate(orientation)
}

function showRemainingCards() {
    $.ajax({
        url: "/cyc/cardcount/",
        type: "GET",
        dataType: "text",
        success: function (cardcount) {
            $('.cardsleft').html("Cards remaining: " + cardcount)
        },
        error: function (jqxhr, errstatus, errmsg) {
            console.error("showRemainingCards function: " + errstatus + " -> " + errmsg)
        }
    })
}

function showCardPossibilities() {
    console.debug("Showing possibilities for cards...")
}

// Helper functions
function getGameStatus() {
    gamestatus = ""
    $.ajax({
        url: "/cyc/gamestatus/",
        type: "GET",
        dataType: "text",
        success: function(result) {
            gamestatus = result
            console.info("Gamestatus: " + gamestatus)
        },
        error: function(jqxhr, errstatus, errmsg) {
            console.error("getGameStatus function: " + errstatus + " -> " + errmsg)
        },
        async: false
    })

    return gamestatus
}

function disable(i) {
    i.prop("disabled", true)
}

function enable(i) {
    i.prop("disabled", false)
}