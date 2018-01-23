package controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc.{AbstractController, ControllerComponents}

@Singleton
class PagesController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  def index = Action { request =>
    Ok(views.html.index("Welcome to City Yandere Carcassonne!"))
  }

  def rules = Action { request =>
    Ok(views.html.rules())
  }
}
