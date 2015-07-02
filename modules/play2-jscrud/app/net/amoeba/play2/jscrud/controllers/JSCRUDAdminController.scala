package net.amoeba.play2.jscrud.controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import play.api.libs.json._
import scala.concurrent.Future
import scala.reflect.runtime.{ universe => ru }
import net.amoeba.play2.jscrud.schema.JsonSchemar
import net.amoeba.play2.jscrud.models.MenuItem
import net.amoeba.play2.jscrud.models.ColumnParam
import net.amoeba.play2.jscrud.models.JSCRUDFormats
import net.amoeba.play2.jscrud.models.JSCRUDParam
import play.api.mvc.Cookie
import play.api.mvc.DiscardingCookie

abstract class JSCRUDAdminController extends Controller { this: JSCRUDSettings =>

  import JSCRUDFormats._

  def title: String
  def menuItem: MenuItem

  def jscrudParam: JSCRUDParam

  def dictionary: Map[String, Map[String, String]] = Map.empty

  def jscrud = Action.async { implicit request =>
    Future.successful(Ok(net.amoeba.play2.jscrud.views.html.index(title, jscrudParam.copy(dictionary = dictionary))))
  }
}

abstract class JSCRUDAdminIndexingController extends Controller { this: JSCRUDSettings =>

  def indexPage = Action.async { implicit request =>
    Future.successful(Ok(net.amoeba.play2.jscrud.views.html.adminHome("JsCRUD Home")))
  }

  lazy val adminCookieName = play.api.Play.current.configuration.getString("auth.cookie.name").getOrElse("play-basic-authentication-filter")
  lazy val logoutRedirect = play.api.Play.current.configuration.getString("auth.admin.logoutredirect").getOrElse("/")

  def redirectLogout = Action.async { request =>
    Future.successful(Redirect(logoutRedirect).discardingCookies(DiscardingCookie(adminCookieName)).withSession(request.session - "play-basic-authentication-filter"))
  }

  def completeLogout = Action { request =>
    Ok("Please close the browser to logout completely.").discardingCookies(DiscardingCookie("play-basic-authentication-filter")).withSession(request.session - "play-basic-authentication-filter")
  }

}

trait JSCRUDSettings {
  def crudControllers: List[JSCRUDAdminController]
  implicit def menuItems: List[MenuItem] = crudControllers.map(_.menuItem)
}
