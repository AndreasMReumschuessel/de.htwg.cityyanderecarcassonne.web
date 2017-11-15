package controllers;

import de.htwg.cityyanderecarcassonne.Carcassonne;
import de.htwg.cityyanderecarcassonne.controller.ICarcassonneController;
import de.htwg.cityyanderecarcassonne.view.tui.TextUI;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.cyc;
import views.html.index;

/**
 * This controller passes the commands to the CityYandereCarcassonne game and returns the TUI.
 */
public class CarcassonneWebController extends Controller {
    private static Carcassonne carcassonne = Carcassonne.getInstance(15, 15, false, true);
    private static ICarcassonneController controller = carcassonne.getController();

    public Result cycarcassonne(String cmd) {
        TextUI tui = carcassonne.getTui();
        tui.processInput(cmd);
        return ok(cyc.render(controller));
    }

    public Result cycarcassonnePost() {
        DynamicForm dynamicForm = Form.form().bindFromRequest();
        String cmd = dynamicForm.get("command");
        return cycarcassonne(cmd);
    }
}
