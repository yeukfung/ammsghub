import play.api.GlobalSettings
import play.api.Logger
import play.api.Application
import events.MessageEventPublisher
import events.MessageCallbackHandler
import play.api.libs.concurrent.Akka
import akka.actor.Props
import actors.CallbackRetryActor
import akka.actor.Cancellable

object Global extends GlobalSettings {

  import play.api.libs.concurrent.Execution.Implicits._
  import scala.concurrent.duration._

  var callbackHandle: Cancellable = null

  override def onStart(app: Application) {
    MessageEventPublisher.subscribe(MessageCallbackHandler)
    val callbackRetryActor = Akka.system(app).actorOf(Props[CallbackRetryActor], name = "callbackRetryActor")
    callbackHandle = Akka.system(app).scheduler.schedule(0.microsecond, 1.minute, callbackRetryActor, "retry_failed")
    
    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
    callbackHandle.cancel()
    MessageEventPublisher.removeSubscriptions()
  }

}