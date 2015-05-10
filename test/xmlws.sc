object xmlws {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet

  val rawXml = <xml>
                 <ToUserName>gh_cf40f8112058</ToUserName>
                 <FromUserName>oA3vqsxEt-CF4wptyEKwpmya4_tM</FromUserName>
                 <CreateTime>1431165762</CreateTime>
                 <MsgType>text</MsgType>
                 <Content>Rtrr</Content>
                 <MsgId>6146810143153862292</MsgId>
               </xml>                             //> rawXml  : scala.xml.Elem = <xml>
                                                  //|                  <ToUserName>gh_cf40f8112058</ToUserName>
                                                  //|                  <FromUserName>oA3vqsxEt-CF4wptyEKwpmya4_tM</FromUserName>
                                                  //|                  <CreateTime>1431165762</CreateTime>
                                                  //|                  <MsgType>text</MsgType>
                                                  //|                  <Content>Rtrr</Content>
                                                  //|                  <MsgId>6146810143153862292</MsgId>
                                                  //|                </xml>

  import scala.xml._
  import play.api.libs.json._
  val xml = rawXml.asInstanceOf[NodeSeq]          //> xml  : scala.xml.NodeSeq = <xml>
                                                  //|                  <ToUserName>gh_cf40f8112058</ToUserName>
                                                  //|                  <FromUserName>oA3vqsxEt-CF4wptyEKwpmya4_tM</FromUserName>
                                                  //|                  <CreateTime>1431165762</CreateTime>
                                                  //|                  <MsgType>text</MsgType>
                                                  //|                  <Content>Rtrr</Content>
                                                  //|                  <MsgId>6146810143153862292</MsgId>
                                                  //|                </xml>

  xml.headOption.map { root =>
    root.child.foldLeft(Json.obj())((acc, item) => acc ++ Json.obj(item.label -> item.text))
  }                                               //> res0: Option[play.api.libs.json.JsObject] = Some({"ToUserName":"gh_cf40f8112
                                                  //| 058","FromUserName":"oA3vqsxEt-CF4wptyEKwpmya4_tM","CreateTime":"1431165762"
                                                  //| ,"MsgType":"text","Content":"Rtrr","MsgId":"6146810143153862292","#PCDATA":"
                                                  //| \n               "})
}