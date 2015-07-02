package models

import play.api.libs.json.JsSuccess
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import play.api.libs.json.JsError
import play.api.libs.json.JsResult
import play.api.libs.json.JsString
import play.api.libs.json.JsValue

trait Enum[A] {
  trait Value { self: A => }
  val values: List[A]
  def parse(v: String): Option[A] = values.find(_.toString().equalsIgnoreCase(v))
}

trait SimpleEnumJson[A] {
  self: Enum[A] =>

  implicit def reads: Reads[A] = new Reads[A] {
    def reads(json: JsValue): JsResult[A] = json match {
      case JsString(v) => parse(v) match {
        case Some(a) => JsSuccess(a)
        case _       => JsError(s"String value ($v) is not a valid enum item ")
      }
      case _ => JsError("String value expected")
    }
  }

  implicit def writes[A]: Writes[A] = new Writes[A] {
    def writes(v: A): JsValue = JsString(v.toString)
  }
}


sealed trait MessageStatus extends MessageStatus.Value
object MessageStatus extends Enum[MessageStatus] with SimpleEnumJson[MessageStatus] {
  case object New extends MessageStatus
  case object Read extends MessageStatus
  case object Notified extends MessageStatus
  case object Error extends MessageStatus
  val values = List(New, Read, Notified, Error)
}


sealed trait ProfileType extends ProfileType.Value
object ProfileType extends Enum[ProfileType] with SimpleEnumJson[ProfileType] {
  case object WeChat extends ProfileType
  val values = List(WeChat)
}

sealed trait MessageType extends MessageType.Value
object MessageType extends Enum[MessageType] with SimpleEnumJson[MessageType] {
  case object Subscribed extends MessageType
  case object MessageArrived extends MessageType
  case object Keywords extends MessageType

  val values = List(Subscribed, MessageArrived, Keywords)
}
