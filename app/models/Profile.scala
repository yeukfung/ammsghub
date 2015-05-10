package models

import scala.concurrent.Future
import play.api.libs.json.Json
import crud.reactivemongo.WeChatProfileCRUD
import scala.concurrent.ExecutionContext.Implicits._
import ModelFormats._

trait Profile {
  def profileType: ProfileType
}

case class WeChatProfile(
  appId: String,
  secret: String,
  token: String,
  aesKey: Option[String],
  isActive: Boolean,
  description: Option[String],
  callbackUrl: Option[String],
  profileType: ProfileType = ProfileType.WeChat) extends Profile

object Profile {

  def getByAppId(appId: String) = {
    val q = Json.obj("appId" -> appId)
    WeChatProfileCRUD.res.find(q).map(_.headOption)
  }

  def getAllActiveProfileWithCallbackUrls() = {
    val q = Json.obj("isActive" -> true, "callbackUrl" -> Json.obj("$exists" -> true))
    WeChatProfileCRUD.res.find(q)
  }
}