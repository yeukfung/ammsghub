package crud.reactivemongo

import play.autosource.reactivemongo.ReactiveMongoAutoSourceController
import models.WeChatProfile
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import models.ModelFormats._

object WeChatProfileCRUD extends ReactiveMongoAutoSourceController[WeChatProfile] {
  def coll = db.collection[JSONCollection]("wechat_profiles")
}