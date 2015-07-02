package crud.reactivemongo

import play.autosource.reactivemongo.ReactiveMongoAutoSourceController
import models.AutoResponse
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import models.ModelFormats._
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType

object AutoResponseCRUD extends AutoResponseCRUDMixin 

trait AutoResponseCRUDMixin extends ReactiveMongoAutoSourceController[AutoResponse] with EnsureIndexMixin {
  def coll = db.collection[JSONCollection]("wechat_autoresponses")

  def ensureIndex() {

  }
}
