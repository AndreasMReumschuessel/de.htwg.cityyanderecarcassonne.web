package controllers;

import de.htwg.cityyanderecarcassonne.Carcassonne;
import de.htwg.cityyanderecarcassonne.controller.ICarcassonneController;
import de.htwg.cityyanderecarcassonne.model.ICard;
import de.htwg.cityyanderecarcassonne.model.IPlayer;
import de.htwg.cityyanderecarcassonne.model.IPosition;
import de.htwg.cityyanderecarcassonne.view.tui.TextUI;
import javafx.geometry.Pos;
import models.CurrentCard;
import models.Player;
import models.PossCardPos;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.cyc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This controller passes the commands to the CityYandereCarcassonne game and returns the TUI.
 */
public class CarcassonneWebController extends Controller {
    private final static Carcassonne carcassonne = Carcassonne.getInstance(15, 15, false, true);
    private final static ICarcassonneController controller = carcassonne.getController();

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

        Player jsonPlayer = new Player();
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

    public Result getCurrentPlayerName() {
        return ok(controller.getCurrentPlayer().getName());
    }

    public Result getCurrentCard() {
        ICard currCard = controller.cardOnHand();

        CurrentCard jsonCurrCard = new CurrentCard();
        jsonCurrCard.cardname = currCard.toString().substring(currCard.toString().lastIndexOf(" ") + 1);
        jsonCurrCard.orientation = currCard.getOrientation();

        return ok(Json.toJson(jsonCurrCard));
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
}
