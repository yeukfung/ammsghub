package models

import org.joda.time.DateTime
import play.api.libs.json.JsObject
import reactivemongo.bson.BSONObjectID
import play.api.libs.json.Json
import crud.reactivemongo.WeChatMessageCRUD
import ModelFormats._
import play.api.libs.ws.WS
import play.api.libs.ws.WSResponse
import org.apache.http.HttpStatus
import play.api.libs.json.JsArray
import play.api.Logger

trait Message {
  def raw: Option[String]
  def status: MessageStatus
  def contentType: String
  def created: DateTime
  def modified: Option[DateTime]
}

object Message {
  import ModelFormats._
  import scala.concurrent.ExecutionContext.Implicits._
  import play.api.Play.current

  def updateMsgStatus(msgId: String, status: MessageStatus) = {
    val id = BSONObjectID(msgId)
    val upd = Json.obj("status" -> status, "modified" -> DateTime.now())
    WeChatMessageCRUD.res.updatePartial(id, upd)
  }

  def getNewMessageById(msgId: String) = {
    WeChatMessageCRUD.res.get(BSONObjectID(msgId)).map {
      case res @ Some((m, id)) if m.status == MessageStatus.New => res
    }
  }

  def findAllNewMessage(list: List[(WeChatProfile, BSONObjectID)]) = {
    val orQuery = Json.obj("status" -> MessageStatus.New, "appId" -> Json.obj("$in" -> JsArray(list.map(x => Json.toJson(x._1.appId)))))
    WeChatMessageCRUD.res.find(orQuery)
  }

  def processCallback(appId: String, msgId: String) = {
    for {
      Some(p) <- Profile.getByAppId(appId)
      Some(msg) <- Message.getNewMessageById(msgId)
    } {
      p._1.callbackUrl.map { url =>
        Logger.info(s"invoking callback url to: ${url} for appId: ${appId}")
        WS.url(url).withQueryString(("msgId", msgId)).get().map {
          case resp: WSResponse =>
            if (resp.status == HttpStatus.SC_OK)
              updateMsgStatus(msgId, MessageStatus.Notified)
          case _ =>
        }
      }
    }
  }

  def readMessage(appId: String, msgId: String) = {
    WeChatMessageCRUD.res.get(BSONObjectID(msgId)) map {
      case res @ Some((m, id)) if m.appId == appId =>
        if (m.status != MessageStatus.Read) updateMsgStatus(msgId, MessageStatus.Read)
        res
    }
  }
}

case class WeChatMessage(
  appId: String,
  raw: Option[String],
  json: Option[JsObject],
  status: MessageStatus,
  contentType: String,
  created: DateTime,
  modified: Option[DateTime]) extends Message


object WeChatMessage {
  def xmlText(fromUserId:String, toUserId:String, createTime:String, content:String) = <xml>
<ToUserName>{toUserId}</ToUserName>
<FromUserName>{fromUserId}</FromUserName>
<CreateTime>{createTime}</CreateTime>
<MsgType>text</MsgType>
<Content>{content}</Content>
</xml>

}

import amcore.play2.PlayMixin
object MessageCache extends PlayMixin {
  import org.joda.time.DateTime

  lazy val expireInHour = conf.getInt("app.messagecache.expireInHour").getOrElse(1)
  private var userMsgMap:Map[String, DateTime] = Map.empty

  def lookup(u:String) = {
    cleanExpired()
    userMsgMap.get(u).isDefined
  }

  def mark(u:String) = {
    val newExpiry = DateTime.now.plusHours(expireInHour)
    userMsgMap = userMsgMap + (u -> newExpiry)
  }

  private def cleanExpired() = {
    userMsgMap = userMsgMap.filter { case (key, expireTime) => DateTime.now.isBefore(expireTime) }
  }
}
