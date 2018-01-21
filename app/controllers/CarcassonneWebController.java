package controllers;

import de.htwg.cityyanderecarcassonne.Carcassonne;
import de.htwg.cityyanderecarcassonne.controller.ICarcassonneController;
import de.htwg.cityyanderecarcassonne.model.ICard;
import de.htwg.cityyanderecarcassonne.model.IPlayer;
import de.htwg.cityyanderecarcassonne.model.IPosition;
import de.htwg.cityyanderecarcassonne.view.tui.TextUI;
import models.*;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.cyc;

import java.util.*;

/**
 * This controller passes the commands to the CityYandereCarcassonne game and returns the TUI.
 */
public class CarcassonneWebController extends Controller {
    private final static Carcassonne carcassonne = Carcassonne.getInstance(15, 15, false, true);
    private final static ICarcassonneController controller = carcassonne.getController();

    private Map<String, String> playerIdMap = new HashMap<>();

    private void execCmd(String cmd) {
        TextUI tui = carcassonne.getTui();
        tui.processInput(cmd);
    }

    public Result cycarcassonne(String cmd) {
        execCmd(cmd);
        return ok(cyc.render(controller));
    }

    public Result cycarcassonneJson(String cmd){
        execCmd(cmd);
        return json();
    }

    public Result json() {
        return ok();
    }

    public Result getGameStatus() {
        return ok(controller.getStatus().toString());
    }

    public Result addPlayer(String name) {
        controller.addPlayer(name);

        String pid = "pid_" + playerIdMap.size();
        playerIdMap.put(name, pid);

        IPlayer controllerPlayer = controller.getPlayers().get(controller.getPlayers().size() - 1);

        Player jsonPlayer = new Player();
        jsonPlayer.pid = pid;
        jsonPlayer.name = name;
        jsonPlayer.meeple = controllerPlayer.getSumMeeples();
        jsonPlayer.score = controllerPlayer.getScore();

        return ok(Json.toJson(jsonPlayer));
    }

    public Result createGame() {
        controller.create();
        controller.startRound();
        return ok();
    }

    public Result getCurrentPlayerId() {
        return ok(playerIdMap.get(controller.getCurrentPlayer().getName()));
    }

    public Result getCurrentCard() {
        return ok(Json.toJson(cardConvert(controller.cardOnHand())));
    }

    public Result rotateCard(String direction) {
        if ("left".equals(direction)) {
            controller.rotateCardLeft();
        } else if ("right".equals(direction)) {
            controller.rotateCardRight();
        } else {
            return badRequest("Unknown direction. Only \"left\" and \"right\" possible.");
        }
        return getCurrentCard();
    }

    private CurrentCard cardConvert(ICard icard) {
        CurrentCard currCard = new CurrentCard();
        currCard.cardname = cardConvertName(icard);
        currCard.orientation = icard.getOrientation();

        return currCard;
    }

    private String cardConvertName(ICard icard) {
        return icard.toString().substring(icard.toString().lastIndexOf(" ") + 1);
    }

    public Result getRemainingCards() {
        return ok(String.valueOf(controller.getRemainingCards()));
    }

    public Result getCardPossibilities() {
        Map<IPosition, String> possMap = controller.getCardPossibilitiesMap(controller.cardOnHand());
        List<PossCardPos> possList = new ArrayList<>();
        for (Map.Entry<IPosition, String> entry : possMap.entrySet()){
            PossCardPos cardPos = new PossCardPos();
            cardPos.selector = entry.getValue();
            cardPos.position = entry.getKey().getY() + "_" + entry.getKey().getX();
            possList.add(cardPos);
        }

        return ok(Json.toJson(possList));
    }

    public Result placeCard(String selection) {
        controller.placeCard(controller.cardOnHand(), selection);
        return getGameStatus();
    }

    public Result getTownsquare() {
        de.htwg.cityyanderecarcassonne.model.townsquare.Townsquare cts = controller.getTownsquare();

        Townsquare ts = new Townsquare();
        ts.cards = new ArrayList<>();
        for (int x = 0; x < cts.getDimX(); x++) {
            for (int y = 0; y < cts.getDimY(); y++) {
                ICard ctsCard = cts.getCard(x, y);
                if (ctsCard != null) {
                    TSCard card = new TSCard();
                    card.position = y + "_" + x;
                    card.name = cardConvertName(ctsCard);
                    card.orientation = ctsCard.getOrientation();
                    ts.cards.add(card);
                }
            }
        }

        return ok(Json.toJson(ts));
    }

    public Result finishRound() {
        controller.finishRound();
        return ok();
    }
}
