package models

import play.api.libs.json.Json
import play.api.libs.json.Format

object ModelFormats {

  implicit val profileTypeFormat = Format(ProfileType.reads, ProfileType.writes)
  implicit val messageTypeFormat = Format(MessageType.reads, MessageType.writes)
  
  implicit val messageStatusFormat = Format(MessageStatus.reads, MessageStatus.writes)
  
  implicit val wechatMessageFormat = Json.format[WeChatMessage]
  implicit val wechatProfileFormat = Json.format[WeChatProfile]
  
  implicit val autoResponseFormat = Json.format[AutoResponse]
}
