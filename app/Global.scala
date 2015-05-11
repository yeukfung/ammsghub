import play.api.GlobalSettings
import play.api.Logger
import play.api.Application
import events.MessageEventPublisher
import events.MessageCallbackHandler
import play.api.libs.concurrent.Akka
import akka.actor.Props
import actors.CallbackRetryActor
import akka.actor.Cancellable
import actors.HouseKeepingActor
import crud.reactivemongo.EnsureIndexMixin
import crud.reactivemongo.WeChatProfileCRUD
import crud.reactivemongo.WeChatMessageCRUD

object Global extends GlobalSettings {

  import play.api.libs.concurrent.Execution.Implicits._
  import scala.concurrent.duration._

  var callbackHandle: Cancellable = null
  var housekeepHandle: Cancellable = null

  override def onStart(app: Application) {
    MessageEventPublisher.subscribe(MessageCallbackHandler)
    val callbackRetryActor = Akka.system(app).actorOf(Props[CallbackRetryActor], name = "callbackRetryActor")
    val housekeepingActor = Akka.system(app).actorOf(Props[HouseKeepingActor], name = "housekeepingActor")
    callbackHandle = Akka.system(app).scheduler.schedule(1.second, 1.minute, callbackRetryActor, "retry_failed")
    housekeepHandle = Akka.system(app).scheduler.schedule(1.minute, 1.hour, housekeepingActor, "housekeep")

    val ensureIndexes: List[EnsureIndexMixin] = List(WeChatMessageCRUD, WeChatProfileCRUD)
    ensureIndexes.foreach { _.ensureIndex() }

    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
    callbackHandle.cancel()
    housekeepHandle.cancel()
    MessageEventPublisher.removeSubscriptions()
  }

}