package controllers;

import de.htwg.cityyanderecarcassonne.Carcassonne;
import de.htwg.cityyanderecarcassonne.view.tui.TextUI;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.cyc;
import views.html.index;

/**
 * This controller passes the commands to the CityYandereCarcassonne game and returns the TUI.
 */
public class CarcassonneWebController extends Controller {
    String fullPrint = "";

    public Result index() {
        return ok(index.render("Welcome to City Yandere Carcassonne!"));
    }

    public Result cycarcassonne(String cmd) {
        TextUI tui = Carcassonne.getInstance(15, 15, false, true).getTui();
        tui.processInput(cmd);
        fullPrint += tui.getTuiString().replace("\n", "<br>").replace(" ", "&nbsp;") + "<br>";
        return ok(cyc.render(fullPrint));
    }
}
