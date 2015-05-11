package crud.reactivemongo

import play.autosource.reactivemongo.ReactiveMongoAutoSourceController
import models.WeChatMessage
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import models.ModelFormats._
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType
import play.api.libs.json.Json
import org.joda.time.DateTime

object WeChatMessageCRUD extends ReactiveMongoAutoSourceController[WeChatMessage] with EnsureIndexMixin {
  def coll = db.collection[JSONCollection]("wechat_messages")

  def ensureIndex() {
    coll.indexesManager.ensure(Index(Seq(("appId", IndexType.Ascending))))
    coll.indexesManager.ensure(Index(Seq(("created", IndexType.Ascending))))
    coll.indexesManager.ensure(Index(Seq(("status", IndexType.Ascending), ("appId", IndexType.Ascending))))
  }

  def removeOldMessagesInDay(days: Int) = {
    val q = Json.obj("created" -> Json.obj("$lt" -> DateTime.now().minusDays(days)))
    res.batchDelete(q)
  }
}