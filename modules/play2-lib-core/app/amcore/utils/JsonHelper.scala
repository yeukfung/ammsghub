package amcore.utils

import play.api.libs.json._

object JsonHelper {

  def js2str(jsValue: JsValue) = jsValue.as[String]

}
