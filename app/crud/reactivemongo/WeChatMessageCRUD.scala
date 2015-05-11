package crud.reactivemongo

import play.autosource.reactivemongo.ReactiveMongoAutoSourceController
import models.WeChatMessage
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import models.ModelFormats._
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType

object WeChatMessageCRUD extends ReactiveMongoAutoSourceController[WeChatMessage] {
  def coll = db.collection[JSONCollection]("wechat_messages")
  coll.indexesManager.ensure(Index(Seq(("appId", IndexType.Ascending))))
  coll.indexesManager.ensure(Index(Seq(("status", IndexType.Ascending), ("appId", IndexType.Ascending))))
}