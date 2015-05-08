package models

trait Profile {
  def profileType: ProfileType
}

case class WeChatProfile(
  appId: String,
  secret: String,
  token: String,
  isActive: Boolean,
  description: Option[String],
  profileType: ProfileType = ProfileType.WeChat) extends Profile


