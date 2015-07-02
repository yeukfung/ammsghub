package controllers

import models._
import crud.reactivemongo._
import scala.concurrent.ExecutionContext.Implicits._
import net.amoeba.play2.jscrud.controllers._
import net.amoeba.play2.jscrud.models._
import models.ModelFormats._
import play.api.libs.json._
import scala.reflect.runtime.{ universe => ru }
import net.amoeba.play2.jscrud.schema.JsonSchemar.JsonTransformHelper

object AutoResponseSchemaGen extends AutoResponseCRUDMixin with JsonSchemaGenMixin {
  def tpe = ru.typeOf[AutoResponse]
  
  import play.api.libs.json._
  import play.api.libs.json.Reads._
  import play.api.libs.functional.syntax._
  import JsonTransformHelper._
  override val schemaTransformer = (__.read[JsObject] and jstType("msgType", "string") ) reduce
  
  override def schemaFormFields: JsArray = {
    val messageTypeList = MessageType.values.foldLeft(Json.arr())((acc, item) => acc.append(Json.obj("value" -> item.toString, "name" -> item.toString)))
    
    Json.parse(s"""[{
     "key": "msgType",
     "type" : "select",
     "titleMap" : ${messageTypeList}
     }, "name", "content", "keywords", "profileIds"]""").as[JsArray]
  }
}

/** Admin Controller **/
trait DefaultJSCRUDSettings extends JSCRUDSettings {
  override val crudControllers =
    AutoResponseCRUDAdminController.asInstanceOf[JSCRUDAdminController] :: Nil
}

object AutoResponseCRUDAdminController extends JSCRUDAdminController with DefaultJSCRUDSettings {
  val title = "Auto Response"
  val menuItem = MenuItem(title, controllers.routes.AutoResponseCRUDAdminController.jscrud().url, "fa-reply-all")

  val jscrudParam = JSCRUDParam(
    schemaUrl = controllers.routes.AutoResponseSchemaGen.genJsSchema().url,
    restUrl = "/api/autoresponse/:id") 
}
