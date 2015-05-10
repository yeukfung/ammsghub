package events

import scala.collection.mutable.Subscriber
import scala.collection.mutable.Publisher

sealed trait MessageEvent

case class MessageArrivedEvent(appId: String, msgId: String) extends MessageEvent

object MessageCallbackHandler extends Subscriber[MessageEvent, Publisher[MessageEvent]] {
  def notify(pub: Publisher[MessageEvent], event: MessageEvent): Unit = {
    event match {
      case MessageArrivedEvent(appId, msgId) =>
        models.Message.processCallback(appId, msgId)
    }
  }
}

object MessageEventPublisher extends Publisher[MessageEvent] {

  def genMessageArrivedEvent(appId: String, msgId: String) = publish(MessageArrivedEvent(appId, msgId))
}