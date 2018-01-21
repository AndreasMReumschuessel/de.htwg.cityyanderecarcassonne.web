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
} else {
    $('#addplayer').hide()
}

if (gamestatus === "ROUND_START") {
    roundStarted()
} else if (gamestatus === "CARD_ROTATED" || gamestatus === "CARD_SET_FAIL") {
    disable($('#roundctrl').html("Finish Round"))

    updateTownsquare()
    showCurrentCard()
    registerRotateCurrentCardListener()
    showCardPossibilities()
} else if (gamestatus === "CARD_SET_SUCCESS") {
    cardSuccessfullySet()
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
    } else if (gamestatus === "CARD_SET_SUCCESS") {
        $.ajax({
            url: "/cyc/finishround/",
            type: "GET",
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

    disable($('#roundctrl').html("Finish Round"))

    updateTownsquare()
    showActivePlayer()
    showCurrentCard()
    registerRotateCurrentCardListener()
    showRemainingCards()
    showCardPossibilities()
}

function cardSuccessfullySet() {
    enable($('#roundctrl').html("Finish Round"))
    disable($('#rotleft'))
    disable($('#rotright'))

    // Clear all old possibilities
    $('.tsColumn').removeClass("active")

    updateTownsquare()
    //showMeeplePossibilities()
}

function showActivePlayer() {
    $.ajax({
        url: "/cyc/currentplayer/",
        type: "GET",
        dataType: "text",
        success: function(result) {
            console.debug("Currentplayer: " + result)
            $('.player').removeClass("active")
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

            createOrUpdateCardImageObject(".currentcard", currCard.cardname)
            rotateCard(".currentcard", currCard.orientation)
        },
        error: function (jqxhr, errstatus, errmsg) {
            console.error("showCurrentCard function: " + errstatus + " -> " + errmsg)
        }
    })
}

function createOrUpdateCardImageObject(div, cardName) {
    var image = $(div + ' > img')
    if (image.length === 0) {
        cardimage = $(document.createElement('img'))
            .addClass('img-responsive')
            .attr("src", "/assets/cyc-data/" + cardName + ".png")
        $(div).append(cardimage)
    } else {
        image.attr("src", "/assets/cyc-data/" + cardName + ".png")
    }
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

function registerRotateCurrentCardListener() {
    $('#rotleft').off()
    $('#rotright').off()

    $('#rotleft').click(function (ev) {
        ajaxRotateCard("left")
    })

    $('#rotright').click(function (ev) {
        ajaxRotateCard("right")
    })
}

function ajaxRotateCard(direction) {
    $.ajax({
        url: "/cyc/rotatecard/" + direction,
        type: "GET",
        dataType: "json",
        success: function (card) {
            rotateCard(".currentcard", card.orientation)
        },
        error: function (jqxhr, errstatus, errmsg) {
            console.error("ajaxRotateCard function " + direction +": " + errstatus + " -> " + errmsg)
        }
    })
}

function showCardPossibilities() {
    $.ajax({
        url: "/cyc/cardposslist/",
        type: "GET",
        dataType: "json",
        success: function (possList) {
            possList.forEach(function (item) {
                $('#' + item.position)
                    .addClass("active")
                    .prop("title", item.selector)
            })
            registerPossibleCardPlacementListener()
        },
        error: function (jqxhr, errstatus, errmsg) {
            console.error("showCardPossibilities function: " + errstatus + " -> " + errmsg)
        }
    })
}

function registerPossibleCardPlacementListener() {
    $('.tsColumn.active').off()
    $('.tsColumn.active').click(function (ev) {
        var selection = $(ev.target).attr("title")
        $.ajax({
            url: "/cyc/placecard/" + selection,
            type: "GET",
            success: function (result) {
                if (result === "CARD_SET_SUCCESS") {
                    cardSuccessfullySet()
                } else {
                    alert("Card cannot be placed here, please rotate the card until it fits ^-^")
                }
            },
            error: function (jqxhr, errstatus, errmsg) {
                console.error("showRemainingCards function: " + errstatus + " -> " + errmsg)
            }
        })
    })
}

function updateTownsquare() {
    $.ajax({
        url: "/cyc/gettownsquare/",
        type: "GET",
        dataType: "json",
        success: function(ts) {
            ts.cards.forEach(function (card) {
                var div = "#" + card.position
                createOrUpdateCardImageObject(div, card.name)
                rotateCard(div, card.orientation)
            })
        },
        error: function(jqxhr, errstatus, errmsg) {
            console.error("getGameStatus function: " + errstatus + " -> " + errmsg)
        },
    })
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

function rotateCard(divid, orientation) {
    $(divid + ' > img').rotate(orientation)
}