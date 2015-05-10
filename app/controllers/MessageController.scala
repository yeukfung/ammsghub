package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import scala.concurrent.Future
import play.api.Logger
import play.api.mvc.Request
import models.WeChatMessage
import models.Message
import models.MessageStatus
import org.joda.time.DateTime
import crud.reactivemongo.WeChatMessageCRUD
import models.ModelFormats
import play.api.libs.json.Json
import play.api.libs.json.Writes
import reactivemongo.bson.BSONObjectID
import play.api.libs.json.JsObject
import scala.xml.NodeSeq

trait RequestInfoMixin {
  def printReqInfo[T](request: Request[T]) {
    Logger.info(s"queryString: ${request.queryString.toString}")
    Logger.info(s"requestBody: ${request.body.toString}")
    Logger.info(s"requestTag: ${request.tags.toString}")
    Logger.info(s"requestMethod: ${request.method}")
    Logger.info(s"requestPath: ${request.path}")
    Logger.info(s"requestContentType: ${request.contentType}")

  }
}

object WxConstants {
  val PARAM_ECHOSTRING = "echostr"
  val PARAM_SIGNATURE = "signature"
  val PARAM_TIMESTAMP = "timestamp"
  val PARAM_NONCE = "nonce"

}

object MessageController extends Controller with RequestInfoMixin {

  import ModelFormats._
  import scala.concurrent.ExecutionContext.Implicits._
  import play.api.libs.json._
  import play.api.libs.functional.syntax._
  import play.api.libs.json.extensions._

  import WxConstants._
  val writer: Writes[WeChatMessage] = ModelFormats.wechatMessageFormat

  private implicit val writerWithId = Writes[(WeChatMessage, BSONObjectID)] {
    case (t, id) =>
      val ser = writer.writes(t).as[JsObject].updateAllKeyNodes {
        case (_ \ "_id", value) => ("id" -> value \ "$oid")
      }
      if ((__ \ "id")(ser).isEmpty) ser.as[JsObject] ++ Json.obj("id" -> id.stringify)
      else ser
  }

  def getMessagesByAppId(appId: String) = Action.async { implicit req =>
    val q = Json.obj("appId" -> appId)
    WeChatMessageCRUD.res.find(q, 500, 0).map { s =>
      Ok(Json.toJson(s))
    }
  }

  def readMessage(appId: String, msgId: String) = Action.async { implicit req =>
    Message.readMessage(appId, msgId).map {
      case Some(x) => Ok(Json.toJson(x))
    }
  }

  def ackMessage(appId: String, msgId: String) = Action.async {
    val q = Json.obj("appId" -> appId, "msgId" -> msgId)
    WeChatMessageCRUD.res.find(q, 1, 0).map {
      case l if l.size > 0 =>
        Message.updateMsgStatus(msgId, MessageStatus.Read)
        Ok("ok")
      case _ => BadRequest("invalid msgId")
    }
  }

  def ackMessages(appId: String) = Action.async { request =>
    val result = request.getQueryString("msgIds").map(_.split(",")).map { msgIds =>
      msgIds.map { msgId =>
        val q = Json.obj("appId" -> appId, "msgId" -> msgId)
        WeChatMessageCRUD.res.find(q, 1, 0).map {
          case l if l.size > 0 =>
            Message.updateMsgStatus(msgId, MessageStatus.Read)
        }
      }
      Ok("ok")
    } getOrElse (BadRequest("error"))
    Future.successful(result)
  }

  private def mapXmlToJson(xml: NodeSeq) = xml.headOption.map { root =>
    root.child.foldLeft(Json.obj())((acc, item) => acc ++ Json.obj(item.label -> item.text))
  }

  def eventReceiver(appId: String) = Action.async { implicit request =>

    val token = play.api.Play.current.configuration.getString(s"wx.${appId}.token").getOrElse("123dakZFe2d")

    printReqInfo(request)
    val echoString = request.getQueryString(PARAM_ECHOSTRING).getOrElse("")

    /**
     * 加密/校验流程如下：
     * 1. 将token、timestamp、nonce三个参数进行字典序排序
     * 2. 将三个参数字符串拼接成一个字符串进行sha1加密
     * 3. 开发者获得加密后的字符串可与signature对比，标识该请求来源于微信
     */
    val isValid = for {
      ts <- request.getQueryString(PARAM_TIMESTAMP)
      nonce <- request.getQueryString(PARAM_NONCE)
      signature <- request.getQueryString(PARAM_SIGNATURE)
    } yield {
      val concatString = token + ts + nonce
      signature == play.api.libs.Codecs.sha1(concatString)
    }

    Logger.info(s"isValud: ${isValid}")
    isValid.map {
      case _ =>
        /**
         * Event will be received like this:
         *
         * [info] application - queryString: Map(signature -> Buffer(d2092d5c27f42ef22b0988598daa23a3bf5e5ff5), timestamp -> Buffer(1431162851), nonce -> Buffer(433987841))
         * [info] application - requestBody: AnyContentAsXml(<xml><ToUserName>gh_cf40f8112058</ToUserName>
         * <FromUserName>oA3vqsxEt-CF4wptyEKwpmya4_tM</FromUserName>
         * <CreateTime>1431162851</CreateTime>
         * <MsgType>event</MsgType>
         * <Event>subscribe</Event>
         * <EventKey/>
         * </xml>)
         * [info] application - requestTag: Map(ROUTE_COMMENTS -> , ROUTE_PATTERN -> /wx/$appId<[^/]+>, ROUTE_CONTROLLER -> controllers.MessageController, ROUTE_ACTION_METHOD -> eventReceiver, ROUTE_VERB -> POST)
         * [info] application - requestMethod: POST
         * [info] application - requestPath: /wx/wx98fe83dda298d2d8
         * [info] application - requestContentType: Some(text/xml)
         * [info] application - isValud: Some(true)
         */
        val contentType = request.contentType.getOrElse("")
        val msg = WeChatMessage(appId = appId,
          raw = request.body.asXml.map(_.toString),
          json = request.body.asXml.flatMap(mapXmlToJson(_)),
          status = MessageStatus.New,
          contentType = contentType,
          created = DateTime.now(),
          modified = Some(DateTime.now()))

        WeChatMessageCRUD.res.insert(msg).map { _ =>
          Ok(echoString)
        }
    } getOrElse Future.successful(BadRequest(""))

  }

}