package actors

import akka.actor.Actor
import models.Profile
import scala.concurrent.ExecutionContext.Implicits._
import models.Message

class CallbackRetryActor extends Actor {

  def receive = {
    case "retry_failed" =>
      for {
        profiles <- Profile.getAllActiveProfileWithCallbackUrls()
        msg <- Message.findAllNewMessage(profiles.toList)
      } {
        msg.foreach {
          case (msg, id) => Message.processCallback(msg.appId, id.stringify)
        }
      }
  }
}