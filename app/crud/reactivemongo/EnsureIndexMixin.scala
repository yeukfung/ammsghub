package crud.reactivemongo

import play.autosource.reactivemongo.ReactiveMongoAutoSourceController

trait EnsureIndexMixin {
  def ensureIndex():Unit
}