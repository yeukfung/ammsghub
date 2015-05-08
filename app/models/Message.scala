package models

import org.joda.time.DateTime

trait Message {
  def raw: String
  def status: MessageStatus
  def contentType: String
  def created: DateTime
}

case class WeChatMessage(
  appId: String,
  raw: String,
  status: MessageStatus,
  contentType: String,
  created: DateTime) extends Message