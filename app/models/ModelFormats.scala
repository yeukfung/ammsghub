package models

import play.api.libs.json.Json
import play.api.libs.json.Format

object ModelFormats {

  implicit val profileTypeFormat = Format(ProfileType.reads, ProfileType.writes)
  implicit val wechatProfileFormat = Json.format[WeChatProfile]
  
  implicit val messageStatusFormat = Format(MessageStatus.reads, MessageStatus.writes)
  implicit val wechatMessageFormat = Json.format[WeChatMessage]
}