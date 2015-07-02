package net.amoeba.play2.jscrud.models

import play.api.libs.json.JsObject
import play.api.libs.json.Json

case class MenuItem(title: String, url: String, icon: String)

case class ColumnParam(title: String, transform: Option[JsObject] = None)

case class JSCRUDParam(
  restUrl: String,
  schemaUrl: String,
  columns: Map[String, ColumnParam] = Map.empty,
  dictionary: Map[String, Map[String, String]] = Map.empty) {

  def columnsAsJson = Json.toJson(columns.foldLeft(Json.obj())((acc, item) => acc ++ Json.obj(item._1 -> Json.toJson(item._2)(JSCRUDFormats.columnParamFormat))))

  def dictionaryAsJson = Json.toJson(dictionary)

}

object JSCRUDFormats {
  implicit val columnParamFormat = Json.format[ColumnParam]
  implicit val jscrudParamFormat = Json.format[JSCRUDParam]
}

