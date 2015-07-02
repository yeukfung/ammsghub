//package net.amoeba.play2.jscrud.libs.autosource
//
//import reactivemongo.bson.BSONObjectID
//import play.autosource.reactivemongo._
//import play.modules.reactivemongo.MongoController
//import play.api.libs.json.Format
//import scala.concurrent.ExecutionContext
//import play.api.mvc.Controller
//import play.modules.reactivemongo.json.collection.JSONCollection
//import play.api.mvc.RequestHeader
//import play.api.libs.iteratee.Enumerator
//import play.api.mvc.BodyParser
//import play.api.libs.iteratee.Done
//import play.api.libs.iteratee.Input
//import scala.concurrent.Future
//import play.mvc.SimpleResult
//import play.autosource.core.AutoSourceController
//import play.api.mvc.Result
//import play.api.libs.json._
//import play.api.libs.functional.syntax._
//import play.api.libs.json.extensions._
//
//abstract class JSCRUDReactiveMongoController[T](implicit ctx: ExecutionContext, format: Format[T])
//  extends AutoSourceController[BSONObjectID]
//  with MongoController with Controller {
//
//  def coll: JSONCollection
//
//  /**
//   * Override this to customize how JsErrors are reported.
//   * The implementation should call onBadRequest
//   */
//  protected def onJsError(request: RequestHeader)(jsError: JsError): Future[Result] =
//    onBadRequest(request, JsError.toFlatJson(jsError).toString)
//
//  /** Override to customize deserialization and add validation. */
//  protected val reader: Reads[T] = format
//  /** Override to customize serialization. */
//  protected val writer: Writes[T] = format
//
//  lazy val res = new ReactiveMongoAutoSource[T](coll)(Format(reader, writer))
//
//  /** Override to cutomize deserialization of queries. */
//  protected val queryReader: Reads[JsObject] = implicitly[Reads[JsObject]]
//
//  /** Override to cutomize deserialization of updates. */
//  protected val updateReader: Reads[JsObject] = implicitly[Reads[JsObject]]
//
//  /** Override to cutomize deserialization of queries and batch updates. */
//  protected val batchReader: Reads[(JsObject, JsObject)] = (
//    (__ \ "query").read(queryReader) and
//    (__ \ "update").read(updateReader)).tupled
//
//  private implicit val writerWithId = Writes[(T, BSONObjectID)] {
//    case (t, id) =>
//      val ser = writer.writes(t).as[JsObject].updateAllKeyNodes {
//        case (_ \ "_id", value) => ("id" -> value \ "$oid")
//      }
//      if ((__ \ "id")(ser).isEmpty) ser.as[JsObject] ++ Json.obj("id" -> id.stringify)
//      else ser
//  }
//  private implicit val idWriter = Writes[BSONObjectID] { id =>
//    Json.obj("id" -> id.stringify)
//  }
//
//  private def bodyReader[A](reader: Reads[A]): BodyParser[A] =
//    BodyParser("ReactiveMongoAutoSourceController body reader") { request =>
//      parse.json(request) mapM {
//        case Right(jsValue) =>
//          jsValue.validate(reader) map { a =>
//            Future.successful(Right(a))
//          } recoverTotal { jsError =>
//            onJsError(request)(jsError) map Left.apply
//          }
//        case left_simpleResult =>
//          Future.successful(left_simpleResult.asInstanceOf[Either[Result, A]])
//      }
//    }
//
//  override def insert =
//    insertAction.async(bodyReader(reader)) { request =>
//      res.insert(request.body) map { id =>
//        Ok(Json.toJson(id))
//      }
//    }
//
//  override def get(id: BSONObjectID) =
//    getAction.async {
//      res.get(id) map {
//        case None      => NotFound(s"ID ${id.stringify} not found")
//        case Some(tid) => Ok(Json.toJson(tid))
//      }
//    }
//
//  override def delete(id: BSONObjectID) =
//    deleteAction.async {
//      res.delete(id) map { _ => Ok(Json.toJson(id)) }
//    }
//
//  override def update(id: BSONObjectID) =
//    updateAction.async(bodyReader(reader)) { request =>
//      res.update(id, request.body) map { _ => Ok(Json.toJson(id)) }
//    }
//
//  override def updatePartial(id: BSONObjectID) =
//    updateAction.async(bodyReader(updateReader)) { request =>
//      res.updatePartial(id, request.body) map { _ => Ok(Json.toJson(id)) }
//    }
//
//  override def batchInsert =
//    insertAction.async(bodyReader(Reads.seq(reader))) { request =>
//      res.batchInsert(Enumerator.enumerate(request.body)) map { lasterror =>
//        Ok(Json.obj("nb" -> lasterror.updated))
//      }
//    }
//
//  private def requestParser[A](reader: Reads[A], default: A): BodyParser[A] =
//    BodyParser("ReactiveMongoAutoSourceController request parser") { request =>
//      request.queryString.get("q") match {
//        case None =>
//          if (request.contentType.exists(m => m.equalsIgnoreCase("text/json")
//            || m.equalsIgnoreCase("application/json")))
//            bodyReader(reader)(request)
//          else
//            Done(Right(default), Input.Empty)
//        case Some(Seq(str)) =>
//          parse.empty(request) mapM { _ =>
//            try {
//              Json.parse(str).validate(reader) map { a =>
//                Future.successful(Right(a))
//              } recoverTotal { jsError =>
//                onJsError(request)(jsError) map Left.apply
//              }
//            } catch {
//              // catch exceptions from Json.parse
//              case ex: java.io.IOException =>
//                onBadRequest(request, "Expecting Json value for query parameter 'q'!") map Left.apply
//            }
//          }
//        case Some(seq) =>
//          parse.empty(request) mapM { _ =>
//            onBadRequest(request, "Expecting single value for query parameter 'q'!") map Left.apply
//          }
//      }
//    }
//
//  private def extractQueryStringInt(request: RequestHeader, param: String): Int =
//    request.queryString.get(param) match {
//      case Some(Seq(str)) =>
//        try { str.toInt } catch { case ex: NumberFormatException => 0 }
//      case _ => 0
//    }
//
//  override def find =
//    getAction.async(requestParser(queryReader, Json.obj())) { request =>
//      val query = request.body
//      val limit = extractQueryStringInt(request, "limit")
//      val skip = extractQueryStringInt(request, "skip")
//
//      res.find(query, limit, skip) map { s =>
//        Ok(Json.toJson(s))
//      }
//    }
//
//  override def findStream =
//    getAction.async(requestParser(queryReader, Json.obj())) { request =>
//      val query = request.body
//      val skip = extractQueryStringInt(request, "skip")
//      val pageSize = extractQueryStringInt(request, "pageSize")
//
//      Future.successful {
//        Ok.chunked(
//          res.findStream(query, skip, pageSize)
//            .map(it => Json.toJson(it.toTraversable))
//            .andThen(Enumerator.eof))
//      }
//    }
//
//  override def batchDelete =
//    deleteAction.async(requestParser(queryReader, Json.obj())) { request =>
//      val query = request.body
//      res.batchDelete(query) map { lasterror => Ok(Json.obj("nb" -> lasterror.updated)) }
//    }
//
//  override def batchUpdate =
//    updateAction.async(requestParser(batchReader, Json.obj() -> Json.obj())) { request =>
//      val (q, upd) = request.body
//      res.batchUpdate(q, upd) map { lasterror => Ok(Json.obj("nb" -> lasterror.updated)) }
//    }
//} 
