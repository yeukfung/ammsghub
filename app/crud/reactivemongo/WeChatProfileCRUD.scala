package crud.reactivemongo

import play.autosource.reactivemongo.ReactiveMongoAutoSourceController
import models.WeChatProfile
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import models.ModelFormats._
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType

object WeChatProfileCRUD extends ReactiveMongoAutoSourceController[WeChatProfile] with EnsureIndexMixin {
  def coll = db.collection[JSONCollection]("wechat_profiles")

  def ensureIndex() {
    coll.indexesManager.ensure(Index(Seq(("appId", IndexType.Ascending))))
  }
}