package net.amoeba.play2.jscrud.schema

import play.api.libs.json.JsObject
import scala.reflect.runtime.{ universe => ru }

object JsonSchemar {

  def genSchema[T: ru.TypeTag]: JsObject = CustomAutoSchema.createSchema[T]

  def genSchema(tpe: ru.Type): JsObject = CustomAutoSchema.createSchema(tpe)

  object JsonTransformHelper {

    import play.api.libs.json._
    import play.api.libs.json.Reads._
    import play.api.libs.functional.syntax._

    /**
     *
     * "properties": {
     * "name": {
     * "title": "Name",
     * "description": "Gimme yea name lad",
     * "type": "string",
     * "pattern": "^[^/]*$",
     * "minLength": 2
     * },
     * "invitation": {
     * "type": "string",
     * "format": "html",
     * "title": "Invitation Design",
     * "description": "Design the invitation in full technicolor HTML"
     * },
     */

    val SCHEMA_REQUIRE = "required"
    val SCHEMA_PROPERTIES = "properties"
    val SCHEMA_DESCRIPTION = "description"
    val SCHEMA_TYPE = "type"
    val SCHEMA_PATTERN = "pattern"
    val SCHEMA_MINLENGTH = "minLength"
    val SCHEMA_MAXLENGTH = "maxLength"
    val SCHEMA_FORMAT = "format"
    val SCHEMA_DEFAULT = "default"
    val SCHEMA_ENUM = "enum"

    def jstRequire(flds: String*) = {
      val jsArr = flds.foldLeft(Json.arr())((acc, item) => acc ++ Json.arr(item))
      (__ \ SCHEMA_REQUIRE).json.put(jsArr)
    }

    def jst(fieldName: String, keyName: String, keyValue: JsValue) = {
      (__ \ SCHEMA_PROPERTIES \ fieldName \ keyName).json.put(keyValue)
    }

    def jstDescription(fn: String, fv: String) = jst(fn, SCHEMA_DESCRIPTION, JsString(fv))
    def jstType(fn: String, fv: String) = jst(fn, SCHEMA_TYPE, JsString(fv))
    def jstPattern(fn: String, fv: String) = jst(fn, SCHEMA_PATTERN, JsString(fv))
    def jstMinLength(fn: String, len: Int) = jst(fn, SCHEMA_MINLENGTH, JsNumber(len))
    def jstMaxLength(fn: String, len: Int) = jst(fn, SCHEMA_MAXLENGTH, JsNumber(len))
    def jstFormat(fn: String, fv: String) = jst(fn, SCHEMA_FORMAT, JsString(fv))
    def jstDefault(fn: String, fv: String) = jst(fn, SCHEMA_DEFAULT, JsString(fv))

    def jstEnum(fn: String)(fvs: String*) = {
      val jsArr = fvs.foldLeft(Json.arr())((acc, item) => acc ++ Json.arr(item))
      jst(fn, SCHEMA_ENUM, jsArr)
    }

  }
}

