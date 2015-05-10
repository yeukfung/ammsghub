object xmlws {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(58); 
  println("Welcome to the Scala worksheet");$skip(364); 

  val rawXml = <xml>
                 <ToUserName>gh_cf40f8112058</ToUserName>
                 <FromUserName>oA3vqsxEt-CF4wptyEKwpmya4_tM</FromUserName>
                 <CreateTime>1431165762</CreateTime>
                 <MsgType>text</MsgType>
                 <Content>Rtrr</Content>
                 <MsgId>6146810143153862292</MsgId>
               </xml>

  import scala.xml._
  import play.api.libs.json._;System.out.println("""rawXml  : scala.xml.Elem = """ + $show(rawXml ));$skip(93); 
  val xml = rawXml.asInstanceOf[NodeSeq];System.out.println("""xml  : scala.xml.NodeSeq = """ + $show(xml ));$skip(129); val res$0 = 

  xml.headOption.map { root =>
    root.child.foldLeft(Json.obj())((acc, item) => acc ++ Json.obj(item.label -> item.text))
  };System.out.println("""res0: Option[play.api.libs.json.JsObject] = """ + $show(res$0))}
}
