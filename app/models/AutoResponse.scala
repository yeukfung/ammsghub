package models

import org.joda.time.DateTime
import play.api.libs.json.JsObject
import reactivemongo.bson.BSONObjectID
import play.api.libs.json._
import crud.reactivemongo._
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
 

  import amcore.utils.JsonQueryHelper._
  import scala.concurrent.ExecutionContext.Implicits._
  import scala.concurrent.duration.Duration
  import scala.concurrent.Await

  val dur = Duration(3, "seconds")

  def findBy(profileId:String, msgType:MessageType):Option[AutoResponse] = {
    val q = qAll("profileIds" , Json.arr(JsString(profileId))) ++ qEq("msgType", msgType.toString)
    Await.result(AutoResponseCRUD.res.find(q), dur).headOption.map(_._1)
  }

  def findByKeyword(profileId:String, userMessage:String):Option[AutoResponse] = {

    val q = qEq("profileIds" , profileId) ++ qEq("msgType", MessageType.Keywords.toString)

    val keywordList = Await.result(AutoResponseCRUD.res.find(q), dur)

    keywordList.filter{ case (ar, arId) =>
      ar.keywords.filter(kw => userMessage.toLowerCase.contains(kw.toLowerCase)).size > 0
    }.headOption.map(_._1)
  }
}
