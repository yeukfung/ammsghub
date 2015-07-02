package amcore.elastic

import play.api.mvc._


object Kibana4 {
  
  case class KibanaReport(
    key:String,
    title:String,
    desc:String,
    reportDashboard:String,
    reportParamA:String,
    filters:List[KibanaFilter],
    host:String
  ) {
    def renderiFrame(dateFilter:String = "1w") = 
      s"""
      <iframe src="${host}/#/dashboard/${reportDashboard}?embed&_g=(refreshInterval:(display:Off,pause:!f,section:0,value:0),time:(from:now-${dateFilter},mode:quick,to:now))&_a=${reportParamA}" style="position:absolute; width:100%; height:100%;"></iframe>"
    """

  }



  trait KibanaFilter

  case object DateFilter extends KibanaFilter {
    val defaultValues = List("1h", "12h", "1d", "2d",
      "1w", "2w", "1m", "3m", "6m", "1y")
  }
  case object SearchFilter extends KibanaFilter

  val allFilters = List(DateFilter, SearchFilter)

}

trait KibanaReportModuleConfig {
  import Kibana4._

  def reports:List[KibanaReport]
  def kibanaHost:String
}

trait KibanaReportController extends Controller { this:KibanaReportModuleConfig =>
/*
  def index = Action { request =>
    getIndexLayout(request)
  }

  def view(key:String) = Action { request =>
    reports.filter(_.key == key).headOption.map { r =>
      getViewLayout(r, request)
    } getOrElse (NotFound)
  }
  */
}

