package controllers

import javax.inject.{ Inject, Singleton }

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import org.webjars.play.WebJarsUtil
import play.api.i18n.I18nSupport
import play.api.mvc.{ AbstractController, AnyContent, ControllerComponents }
import utils.auth.DefaultEnv

import scala.concurrent.Future

@Singleton
class PagesController @Inject() (cc: ControllerComponents, silhouette: Silhouette[DefaultEnv])(implicit webJarsUtil: WebJarsUtil, assets: AssetsFinder) extends AbstractController(cc) with I18nSupport {
  def index = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.index("Welcome to City Yandere Carcassonne!")))
  }

  def rules = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.rules()))
  }
}
