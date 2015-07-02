//package net.amoeba.play2.jscrud.libs.autosource
//
//import play.core.Router
//import play.autosource.core.AutoSourceController
//import play.api.mvc.EssentialAction
//import play.api.mvc.PathBindable
//import play.api.mvc.RequestHeader
//import play.api.mvc.Handler
//import reactivemongo.bson.BSONObjectID
//import play.autosource.reactivemongo._
//import play.api.mvc.Controller
//abstract class JSCRUDRouter extends JSCRUDAutoSourceRouter[BSONObjectID]
//
//abstract class JSCRUDAutoSourceRouter[Id](implicit idBindable: PathBindable[Id])
//  extends Router.Routes with Controller {
//
//  def resController: AutoSourceController[Id]
//
//  private var path: String = ""
//
//  private val Slash = "/?".r
//  private val Id = "/([^/]+)/?".r
//  private val Partial = "/([^/]+)/partial".r
//  private val Find = "/find/?".r
//  private val Batch = "/batch/?".r
//  private val Stream = "/stream/?".r
//
//  def withId(id: String, action: Id => EssentialAction) =
//    idBindable.bind("id", id).fold(badRequest, action)
//
//  def setPrefix(prefix: String) {
//    path = prefix
//  }
//
//  def prefix = path
//  def documentation = Nil
//  def routes = new scala.runtime.AbstractPartialFunction[RequestHeader, Handler] {
//    override def applyOrElse[RH <: RequestHeader, H >: Handler](rh: RH, default: RH => H) = {
//      if (rh.path.startsWith(path)) {
//        (rh.method, rh.path.drop(path.length)) match {
//          case ("GET", Stream())      => resController.findStream
//          case ("GET", Id(id))        => withId(id, resController.get)
//          case ("GET", Slash())       => resController.find
//
//          case ("PUT", Batch())       => resController.batchUpdate
//          case ("PUT", Partial(id))   => withId(id, resController.updatePartial)
//          case ("PATCH", Partial(id)) => withId(id, resController.updatePartial)
//          case ("PUT", Id(id))        => withId(id, resController.update)
//          case ("PATCH", Id(id))      => withId(id, resController.update)
//
//          case ("POST", Batch())      => resController.batchInsert
//          case ("POST", Find())       => resController.find
//          case ("POST", Slash())      => resController.insert
//
//          case ("DELETE", Batch())    => resController.batchDelete
//          case ("DELETE", Id(id))     => withId(id, resController.delete)
//          case _                      => default(rh)
//        }
//      } else {
//        default(rh)
//      }
//    }
//
//    def isDefinedAt(rh: RequestHeader) =
//      if (rh.path.startsWith(path)) {
//        (rh.method, rh.path.drop(path.length)) match {
//          case ("GET", Stream() | Id(_) | Slash())   => true
//          case ("PUT", Batch() | Partial(_) | Id(_)) => true
//          case ("PATCH", Partial(_) | Id(_))         => true
//          case ("POST", Batch() | Slash())           => true
//          case ("DELETE", Batch() | Id(_))           => true
//          case _                                     => false
//        }
//      } else {
//        false
//      }
//  }
//}
//
