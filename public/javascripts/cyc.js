// Carcassonne Frontend Logic

// Vue Handling
Vue.component('player-item', {
    props: ['player'],
    template: '<div class="player" :id="player.pid">{{ player.meeple }} : {{ player.name }} : {{ player.score }}</div>'
})

var playerlistVue = new Vue({
    el: '#playerlist',
    data: {
        playerList: []
    }
})

// Get game status and do logic
gamestatus = getGameStatus()

initialRendering()

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
            renderAllPlayers()
            $('#playername').prop("value", "")
            console.debug("AJAX succeeded! Player name: " + result.name)
        },
        error: function(jqxhr, errstatus, errmsg) {
            console.error("Addplayer function: " + errstatus + " -> " + errmsg)
        }
    })
});

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
function checkGameStartable(numPlayers) {
    if (numPlayers > 0) {
        enable($('#roundctrl'))
    }
    if (numPlayers > 3) {
        $('#addplayer').hide()
    }
}

function roundStarted() {
    console.info("A new round has started!")
    $('#addplayer').hide()
    $('.currentcard').show()
    $('.cardsleft').show()
    enable($('#rotleft'))
    enable($('#rotright'))

    $('.meeple.poss').off()
    $('.meeple.poss').remove()

    disable($('#roundctrl').html("Finish Round"))

    updateTownsquare()
    renderAllPlayers()
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
    $('.tsColumn.active').off()
    $('.tsColumn').removeClass("active")

    showMeeplePossibilities()
    updateTownsquare()
}

function meepleSuccessfullySet() {
    $('.meeple.poss').off()
    $('.meeple.poss').remove()

    roundStarted()
}

function initialRendering() {
    renderAllPlayers()
}

function renderAllPlayers() {
    $.ajax({
        url: "/cyc/allplayers/",
        type: "GET",
        dataType: "json",
        success: function(playerlist){
            playerlistVue.playerList = []
            playerlist.forEach(function (player) {
                playerlistVue.playerList.push({pid: player.pid, name: player.name, meeple: player.meeple, score: player.score})
            })
            checkGameStartable(playerlist.length)
        },
        error: function(jqxhr, errstatus, errmsg) {
            console.error("renderAllPlayers function: " + errstatus + " -> " + errmsg)
        }
    })
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
            url: "/cyc/placecard/" + selection + "/" + $(ev.target).attr("id"),
            type: "GET",
            dataType: "text",
            success: function (result) {
                if (result === "CARD_SET_SUCCESS") {
                    cardSuccessfullySet()
                } else {
                    alert("Card cannot be placed here, please rotate the card until it fits ^-^")
                }
            },
            error: function (jqxhr, errstatus, errmsg) {
                console.error("placeCard function: " + errstatus + " -> " + errmsg)
            }
        })
    })
}

function showMeeplePossibilities() {
    $.ajax({
        url: "/cyc/meepleposslist/",
        type: "GET",
        dataType: "json",
        success: function (possList) {
            var cardId = '#' + possList.position
            createMeepleContainer(cardId)
            Object.keys(possList.regions).forEach(function (key, item) {
                showMeepleOnPosition(cardId + ' > .meeplecontainer', key, 'poss', possList.regions[key])
            })
            //$(cardId + ' > .meeplecontainer').rotate(possList.orientation)
            registerPossibleMeeplePlacementListener()
        },
        error: function (jqxhr, errstatus, errmsg) {
            console.error("showMeeplePossibilities function: " + errstatus + " -> " + errmsg)
        }
    })
}

function createMeepleContainer(div) {
    if ($(div + ' > .meeplecontainer').length === 0) {
        $(div).append($(document.createElement('div')).addClass('meeplecontainer'))
    }
}

function showMeepleOnPosition(divid, location, type, id) {
    meeple = $(document.createElement('div'))
                .addClass('meeple')
                .addClass(location)
                .addClass(type)
                .attr("id", id)
    $(divid).append(meeple)
}

function registerPossibleMeeplePlacementListener() {
    $('.meeple.poss').off()
    $('.meeple.poss').click(function (ev) {
        var selection = $(ev.target).attr("id")
        $.ajax({
            url: "/cyc/placemeeple/" + selection,
            type: "GET",
            dataType: "text",
            success: function (result) {
                if (result === "ROUND_START") {
                    meepleSuccessfullySet()
                    console.debug("meeple set succ")
                } else {
                    alert("Meeple cannot be placed here, well, ba-baka this shouldn't happen >///<" + result)
                }
            },
            error: function (jqxhr, errstatus, errmsg) {
                console.error("placeMeeple function: " + errstatus + " -> " + errmsg)
            }
        })
    })
}

function updateTownsquare() {
    $('.meeplecontainer').remove()
    $.ajax({
        url: "/cyc/gettownsquare/",
        type: "GET",
        dataType: "json",
        success: function(ts) {
            ts.cards.forEach(function (card) {
                var div = "#" + card.position
                createOrUpdateCardImageObject(div, card.name)
                rotateCard(div, card.orientation)

                createMeepleContainer(div)
                Object.keys(card.regions).forEach(function (key, item) {
                    showMeepleOnPosition(div + ' > .meeplecontainer', key, card.regions[key])
                })
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