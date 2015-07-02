package amcore.filters

import play.api.Logger
import play.api.Routes
import play.api.mvc._
import scala.concurrent.Future
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

trait AccessLoggingFilterConfig[T] {

  def skipControllers:List[String] = List("assets")
  def skipActions:List[String] = List()

  def convert(req:RequestHeader):Future[Option[T]] 

  def process(req:RequestHeader, t:T):Future[Boolean]
  
}

trait JsonAccessLoggingFilterConfig extends AccessLoggingFilterConfig[JsObject] {

  override def convert(rh:RequestHeader):Future[Option[JsObject]] = Future.successful {
    val controller = rh.tags.get(Routes.ROUTE_CONTROLLER).getOrElse("")
    val action = rh.tags.get(Routes.ROUTE_ACTION_METHOD).getOrElse("")

    val shouldSkip = controller.length == 0 || action.length == 0 || skipControllers.filter(_.trim.length > 0).exists(x => controller.toLowerCase.indexOf(x) >= 0) || skipActions.filter(_.trim.length > 0).exists(x => action.toLowerCase.indexOf(x) >= 0)

//    Logger.debug("should skip = " + shouldSkip);
    if(shouldSkip) 
      None
    else {
      val js = Json.obj(
        "verb" -> rh.tags.get(Routes.ROUTE_VERB),
        "controller" -> controller,
        "action" -> action,
        "pattern" -> rh.tags.get(Routes.ROUTE_PATTERN),
        "url" -> rh.uri,
        "queryString" -> rh.queryString,
        "session" -> rh.session.data
      )
      Some(js.as[JsObject])
    }

  } 
}

class AccessLoggingFilter[T] extends Filter { this:AccessLoggingFilterConfig[T] =>

  def apply(nextFilter: (RequestHeader) => Future[Result])
           (requestHeader: RequestHeader): Future[Result] = {
    for {
      t <- convert(requestHeader) if t.isDefined
      flag <- process(requestHeader, t.get) 
    } {
      if(!flag) {
        Logger.error(s"There is some error when processing the access log")
      }
    }

    nextFilter(requestHeader)
  }

}
