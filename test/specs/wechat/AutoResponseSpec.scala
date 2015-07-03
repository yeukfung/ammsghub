package specs.wechat

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import specs.TestDBMixin

class AutoResponseSpec extends PlaySpecification with TestDBMixin {


//      "reply on message event if profile id exist" in new WithApplication 
  def testReplyOnMessageEvent {
      val sampleIn = <xml>
 <ToUserName>gh_cf40f8112058</ToUserName>
 <FromUserName>oA3vqsxEt-CF4wptyEKwpmya4_t1</FromUserName> 
 <CreateTime>1348831860</CreateTime>
 <MsgType>text</MsgType>
 <Content>this is a test</Content>
 <MsgId>1234567890123456</MsgId>
 </xml>
     
          val req = FakeRequest("POST", "/wx/wx1?signature=d2092d5c27f42ef22b0988598daa23a3bf5e5ff5&timestamp=1431162851&nonce=433987841").withBody(sampleIn)
      val result = route(req).get
      contentAsString(result) must contain("msg arrived") 
      Thread.sleep(1000)
    }   


//      "reply on message event if profile id exist" in new WithApplication 
  def testReplyOnKeyword {
      val sampleIn = <xml>
 <ToUserName>gh_cf40f8112058</ToUserName>
 <FromUserName>oA3vqsxEt-CF4wptyEKwpmya4_t2</FromUserName> 
 <CreateTime>1348831860</CreateTime>
 <MsgType>text</MsgType>
 <Content>money</Content>
 <MsgId>1234567890123456</MsgId>
 </xml>
     
          val req = FakeRequest("POST", "/wx/wx1?signature=d2092d5c27f42ef22b0988598daa23a3bf5e5ff5&timestamp=1431162851&nonce=433987841").withBody(sampleIn)
      val result = route(req).get
      contentAsString(result) must contain("你問多少錢") 
      Thread.sleep(1000)
    }  


  def testNotMatchingKeywords {
   val sampleIn = <xml>
   <ToUserName>gh_cf40f8112058</ToUserName>
   <FromUserName>oA3vqsxEt-CF4wptyEKwpmya4_t3</FromUserName> 
   <CreateTime>1348831860</CreateTime>
   <MsgType>text</MsgType>
   <Content>money</Content>
   <MsgId>1234567890123456</MsgId>
   </xml>
       
            val req = FakeRequest("POST", "/wx/wx3?signature=d2092d5c27f42ef22b0988598daa23a3bf5e5ff5&timestamp=1431162851&nonce=433987841").withBody(sampleIn)
        val result = route(req).get
        contentAsString(result) must contain("msg arrived") 
        Thread.sleep(1000)  
  }

  def testReplyEmptyWhenForMessageArrivedEvent = {
     val sampleIn = <xml>
   <ToUserName>gh_cf40f8112058</ToUserName>
   <FromUserName>oA3vqsxEt-CF4wptyEKwpmya4_tN</FromUserName> 
   <CreateTime>1348831860</CreateTime>
   <MsgType>text</MsgType>
   <Content>msgEvent</Content>
   <MsgId>1234567890123456</MsgId>
   </xml>
       
            val req = FakeRequest("POST", "/wx/wx3?signature=d2092d5c27f42ef22b0988598daa23a3bf5e5ff5&timestamp=1431162851&nonce=433987841").withBody(sampleIn)
        val result = route(req).get
        contentAsString(result) must contain("msg arrived") 
        Thread.sleep(1000)             


            val req1 = FakeRequest("POST", "/wx/wx3?signature=d2092d5c27f42ef22b0988598daa23a3bf5e5ff5&timestamp=1431162851&nonce=433987841").withBody(sampleIn)
        val result1 = route(req1).get
        contentAsString(result1).length must_== 0
        Thread.sleep(1000)             
  }

  "AutoResponse" should {
    
    "reply on subscribed event if profile id exist" in new WithDbData {
      val sampleIn = <xml><ToUserName>gh_cf40f8112058</ToUserName>
          <FromUserName>oA3vqsxEt-CF4wptyEKwpmya4_t0</FromUserName>
          <CreateTime>1431162851</CreateTime>
          <MsgType>event</MsgType>
          <Event>subscribe</Event>
          <EventKey/>
          </xml>

          val req = FakeRequest("POST", "/wx/wx1?signature=d2092d5c27f42ef22b0988598daa23a3bf5e5ff5&timestamp=1431162851&nonce=433987841").withBody(sampleIn)
      val result = route(req).get
      contentAsString(result) must contain("wx1") 
      Thread.sleep(1000)

      testReplyOnMessageEvent

      testReplyOnKeyword

      testNotMatchingKeywords

      testReplyEmptyWhenForMessageArrivedEvent
    }



//    "reply on filtered event if profile id exist and matched" in new WithApplication {
//      
//    }
//
//    "reply empty string if profile id not exist or filtered event not match" in new WithApplication {
//      
//    }
//
//    "reply empty string if last message event of same user arrived within 1 hour" in new WithApplication {
//      
//    }

  }

}
