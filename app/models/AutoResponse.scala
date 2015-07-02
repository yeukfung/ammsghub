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

case class AutoResponse (
  msgType: MessageType,
  content: String,
  name: Option[String],
  keywords: List[String],
  profileIds: List[String]
)


object AutoResponse {
 
  def createOrSave(autoResponse: AutoResponse) = {
     ???
  }

  def deleteById(id:Long) = {
   ???
  }

  def getAllAutoReponse(msgType:MessageType) = {
   ???
  }

}
