package actors

import akka.actor.Actor
import crud.reactivemongo.WeChatMessageCRUD

class HouseKeepingActor extends Actor {

  val DAYS = play.api.Play.current.configuration.getInt("housekeep.days").getOrElse(10)

  def receive = {
    case "housekeep" =>
      WeChatMessageCRUD.removeOldMessagesInDay(DAYS);
  }
}