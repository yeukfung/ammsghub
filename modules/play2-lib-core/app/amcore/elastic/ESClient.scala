package amcore.elastic

import scala.concurrent.Future
import play.api.Logger
import play.api.libs.ws.WS
import play.api.libs.ws.Response
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import org.joda.time.{DateTime, DateTimeZone}


class ESIndex(esClient:ESClient, indexName:String) {

  def index = esClient.index(indexName) _
  def get = esClient.get(indexName) _
  def updatePartial = esClient.updatePartial(indexName) _

}

class ESClient(esURL: String, dataLogger:Option[Logger] = None) {

  import play.api.Play.current

  def baseUrl(idx: String, t: String, action: String) = esURL + s"/$idx/$t/$action".replaceAll("///", "/").replaceAll("//", "/")

  def bulk(index: Option[String] = None, t: Option[String] = None, data: JsObject): Future[Response] = {
    val url = baseUrl(index.getOrElse(""), t.getOrElse(""), "_bulk")
    WS.url(url).post(data)
  }

  def count(indices: Seq[String], types: Seq[String], query: String): Future[Response] = {
    val url = baseUrl(indices.mkString(","), types.mkString(","), "_count")
    WS.url(url).get
  }

  def createIndex(name: String, settings: Option[JsObject] = None): Future[Response] = {
    val url = s"$esURL/$name"
    settings match {
      case Some(js) => WS.url(url).put(js)
      case None => WS.url(url).put(Json.obj())
    }
  }
  
  def createTemplate(templateId:String, template: JsObject): Future[Response] = {
    val url = s"$esURL/_template/$templateId"
    Logger.info(s"creating Template: $url with JS: $template")  
    WS.url(url).put(template)
  }

  def deleteIndex(name: String): Future[Response] = {
    val url = s"$esURL/$name"
    WS.url(url).delete()
  }

  def get(index: String)(`type`: String, id: String): Future[Response] = {
    val url = baseUrl(index, `type`, id)
    WS.url(url).get
  }

  def getMapping(indices: Seq[String], types: Seq[String]): Future[Response] = {
    val url = baseUrl(indices.mkString(","), "_mapping", types.mkString(","))
    WS.url(url).get
  }

  def updatePartial(index:String)(`type`: String, id: String, dataIn:JsObject): Future[Response] = {
    val url = baseUrl(index, `type`, s"$id/_update")
    WS.url(url).post(Json.obj("doc" -> dataIn))
  }

  def index(index: String)(`type`: String, id: Option[String], dataIn: JsObject): Future[Response] = {
    val url = baseUrl(index, `type`, id.getOrElse(""))
    val data = Json.obj(
      "@timestamp" -> DateTime.now.withZone(DateTimeZone.UTC).toString("yyyy-MM-dd'T'HH:mm:ss")) ++ dataIn
    
//    Logger.info(s"indexing data: $data with url: $url")
    dataLogger.map { logger =>
      val dataToLog = s"${`type`},${id.getOrElse("")},$data"
      logger.info(dataToLog)
    }
    id match {
      case Some(id) =>
        if(id == "_mapping") {
          Logger.info(s"mapping data, url = $url with data: $dataIn")
          WS.url(url).put(dataIn)
        } else 
          WS.url(url).put(data)
      case None =>
        WS.url(url).post(data)
    }
  }

  def refresh(index: String) = {
    val url = s"$esURL/$index"
    WS.url(url).post(Json.obj())
  }

  def search(index: String, query: JsObject): Future[Response] = {
    val url = s"$esURL/$index/_search"
    WS.url(url).post(query)
  }
  
  def deleteByQuery(index:String, t:String, queryStr:String): Future[Response] = {
    val url = baseUrl(index, t, s"_query?q=$queryStr")
    WS.url(url).delete()
  }


  def getESIndex(idxName:String): ESIndex = new ESIndex(this, idxName)

  
}

