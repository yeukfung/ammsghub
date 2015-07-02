package net.amoeba.play2.jscrud.controllers

import play.api.mvc.Controller
import scala.reflect.runtime.{ universe => ru }
import play.api.mvc.Action
import scala.concurrent.Future
import net.amoeba.play2.jscrud.schema.JsonSchemar
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.libs.json.JsObject

trait JsonSchemaGenMixin {
  this: Controller =>
  def tpe: ru.Type

  def schemaTransformer: Reads[JsObject] = __.read[JsObject]

  def schemaFormFields: JsArray = Json.parse("""["*"]""").as[JsArray]

  def schemaFormActions: JsArray = Json.parse("""[{
    "type": "actions",
    "items": [{
      "type": "submit",
      "style": "btn-info",
      "title": "Save"
    }, {
      "type": "button",
      "style": "btn-danger",
      "title": "Delete",
      "onClick": "onDelete()"
    }]
  }]""").as[JsArray]

  def schemaForm: JsArray = schemaFormFields ++ schemaFormActions

  def genJsSchema = Action.async {
    Future.successful {
      val schema = Json.obj("schema" -> JsonSchemar.genSchema(tpe).transform(schemaTransformer).get)
      val form = Json.obj("schemaForm" -> schemaForm)
      Ok(schema ++ form)

    }
  }

}
