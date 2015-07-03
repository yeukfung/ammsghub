package specs

import play.api.test._
import play.api.libs.json._
import org.specs2.execute._
import crud.reactivemongo._
import amcore.utils._
import models._
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits._

trait TestDBMixin { this: PlaySpecification =>

  private val idExists = JsonQueryHelper.qExists("_id" , true)

  private val dur = Duration(5, "seconds")

  def loadSampleData() {
    // add 3 WeChat profile wx1, wx2, wx3
    val profiles = for {
      idx <- 1 to 3
    } yield WeChatProfile("wx" + idx,
      "secret" + idx,
      "token" + idx,
      Some("aesKey" + idx),
      true,
      Some("desc" + idx),
      None,
      ProfileType.WeChat)

    profiles.foreach( p => Await.result(WeChatProfileCRUD.res.insert(p), dur))

    Await.result(WeChatProfileCRUD.res.find(Json.obj()), dur).size must_== 3

    
    // add auto response record
    val ap_subscribed_1 = AutoResponse(MessageType.Subscribed, "on subscribed event - wx1", None, List(), List("wx1"))
    val ap_subscribed_23 = AutoResponse(MessageType.Subscribed, "on subscribed event - wx2 and wx3", None, List(), List("wx2", "wx3"))

    val ap_msgarrived_123 = AutoResponse(MessageType.MessageArrived, "on msg arrived, we will response within 24 hours", None, List(), List("wx1", "wx2", "wx3"))

    val ap_keyword_12 = AutoResponse(MessageType.Keywords, "你問多少錢", Some("price tag"), List("price", "money", "錢"), List("wx1", "wx2"))
    
    val ap_keyword_23 = AutoResponse(MessageType.Keywords, "Please wait", Some("bouns point"), List("vip", "bouns", "積分"), List("wx2","wx3"))

    List(ap_subscribed_1, ap_subscribed_23, ap_msgarrived_123, ap_keyword_12, ap_keyword_23).foreach { autoResp =>
      
      Await.result(AutoResponseCRUD.res.insert(autoResp), dur)
    }

    Await.result(AutoResponseCRUD.res.find(Json.obj()), dur).size must_== 5

  }

  def cleanData() = {
      Await.result(WeChatProfileCRUD.res.batchDelete(idExists),dur)
      Await.result(WeChatMessageCRUD.res.batchDelete(idExists),dur)
      Await.result(AutoResponseCRUD.res.batchDelete(idExists),dur)
  }

  abstract class WithDbData extends WithApplication {
    override def around[T: AsResult](t: => T): Result = super.around {
      setupData()
      t
    }
    
    def setupData() {
      cleanData()
      loadSampleData()
    }
  }
  

}
