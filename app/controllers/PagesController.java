package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.rules;

public class PagesController extends Controller {
    public Result index() {
        return ok(index.render("Welcome to City Yandere Carcassonne!"));
    }

    public Result rules() {
        return ok(rules.render());
    }
}
