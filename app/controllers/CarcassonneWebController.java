package controllers;

import de.htwg.cityyanderecarcassonne.Carcassonne;
import de.htwg.cityyanderecarcassonne.controller.ICarcassonneController;
import de.htwg.cityyanderecarcassonne.model.ICard;
import de.htwg.cityyanderecarcassonne.model.IPlayer;
import de.htwg.cityyanderecarcassonne.model.IPosition;
import de.htwg.cityyanderecarcassonne.model.IRegion;
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

    private Map<IPlayer, String> playerIdMap = new HashMap<>();
    private String lastCardPosition;

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

        IPlayer controllerPlayer = controller.getPlayers().get(controller.getPlayers().size() - 1);

        String pid = "pid_" + playerIdMap.size();
        playerIdMap.put(controllerPlayer, pid);

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
        return ok(playerIdMap.get(controller.getCurrentPlayer()));
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
        System.out.println(controller.cardOnHand().getLeftMiddle().getID());
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

    public Result placeCard(String selection, String position) {
        lastCardPosition = position;
        controller.placeCard(controller.cardOnHand(), selection);
        return getGameStatus();
    }

    public Result getMeeplePossibilities() {
        ICard card = controller.cardOnHand();

        Map<IRegion, String> possMap = controller.getRegionPossibilitiesMap(card);

        TSCard tscard = new TSCard();
        tscard.position = lastCardPosition;
        tscard.name = cardConvertName(card);
        tscard.orientation = card.getOrientation();
        tscard.regions = new HashMap<>();
        for (Map.Entry<IRegion, String> entry : possMap.entrySet()) {
            if (entry.getKey().equals(card.getLeftTop()))
                tscard.regions.put("LT", entry.getValue());
            if (entry.getKey().equals(card.getLeftMiddle()))
                tscard.regions.put("LM", entry.getValue());
            if (entry.getKey().equals(card.getLeftBelow()))
                tscard.regions.put("LB", entry.getValue());

            if (entry.getKey().equals(card.getBelowLeft()))
                tscard.regions.put("BL", entry.getValue());
            if (entry.getKey().equals(card.getBelowMiddle()))
                tscard.regions.put("BM", entry.getValue());
            if (entry.getKey().equals(card.getBelowRight()))
                tscard.regions.put("BR", entry.getValue());

            if (entry.getKey().equals(card.getCenterMiddle()))
                tscard.regions.put("C", entry.getValue());

            if (entry.getKey().equals(card.getTopLeft()))
                tscard.regions.put("TL", entry.getValue());
            if (entry.getKey().equals(card.getTopMiddle()))
                tscard.regions.put("TM", entry.getValue());
            if (entry.getKey().equals(card.getTopRight()))
                tscard.regions.put("TR", entry.getValue());;

            if (entry.getKey().equals(card.getRightTop()))
                tscard.regions.put("RT", entry.getValue());
            if (entry.getKey().equals(card.getRightMiddle()))
                tscard.regions.put("RM", entry.getValue());
            if (entry.getKey().equals(card.getRightBelow()))
                tscard.regions.put("RB", entry.getValue());
        }

        return ok(Json.toJson(tscard));
    }

    public Result placeMeeple(String selection) {
        controller.placeMeeple(controller.getCurrentPlayer(), controller.cardOnHand(), selection);
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
                    card.regions = new HashMap<>();

                    if (ctsCard.getLeftTop().getPlayer() != null)
                        card.regions.put("LT", playerIdMap.get(ctsCard.getLeftTop().getPlayer()));
                    if (ctsCard.getLeftMiddle().getPlayer() != null)
                        card.regions.put("LM", playerIdMap.get(ctsCard.getLeftMiddle().getPlayer()));
                    if (ctsCard.getLeftBelow().getPlayer() != null)
                        card.regions.put("LB", playerIdMap.get(ctsCard.getLeftBelow().getPlayer()));

                    if (ctsCard.getTopLeft().getPlayer() != null)
                        card.regions.put("TL", playerIdMap.get(ctsCard.getTopLeft().getPlayer()));
                    if (ctsCard.getTopMiddle().getPlayer() != null)
                        card.regions.put("TM", playerIdMap.get(ctsCard.getTopMiddle().getPlayer()));
                    if (ctsCard.getTopRight().getPlayer() != null)
                        card.regions.put("TR", playerIdMap.get(ctsCard.getTopRight().getPlayer()));

                    if (ctsCard.getCenterMiddle().getPlayer() != null)
                        card.regions.put("C", playerIdMap.get(ctsCard.getCenterMiddle().getPlayer()));

                    if (ctsCard.getBelowLeft().getPlayer() != null)
                        card.regions.put("BL", playerIdMap.get(ctsCard.getBelowLeft().getPlayer()));
                    if (ctsCard.getBelowMiddle().getPlayer() != null)
                        card.regions.put("BM", playerIdMap.get(ctsCard.getBelowMiddle().getPlayer()));
                    if (ctsCard.getBelowRight().getPlayer() != null)
                        card.regions.put("BR", playerIdMap.get(ctsCard.getBelowRight().getPlayer()));

                    if (ctsCard.getRightTop().getPlayer() != null)
                        card.regions.put("RT", playerIdMap.get(ctsCard.getRightTop().getPlayer()));
                    if (ctsCard.getRightMiddle().getPlayer() != null)
                        card.regions.put("RM", playerIdMap.get(ctsCard.getRightMiddle().getPlayer()));
                    if (ctsCard.getRightBelow().getPlayer() != null)
                        card.regions.put("RB", playerIdMap.get(ctsCard.getRightBelow().getPlayer()));

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
