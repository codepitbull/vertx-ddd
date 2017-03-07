package io.vertx.scala.ddd.vertx.eventbus.kryo

import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.scala.core.eventbus.Message
import org.scalatest.{FlatSpec, Matchers}
import io.vertx.scala.ddd.vertx.kryo.KryoEncoding._
import io.vertx.scala.ddd.vertx.kryo.{KryoEncoding, KryoMessage}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar

class KryoEncodingSpec extends FlatSpec with Matchers with MockitoSugar {

  "An unregistered class" should "not be encodable" in {
    val encoding = KryoEncoding(Seq())
    assertThrows[IllegalArgumentException] {
      encoding.encodeToBuffer(new NotRegistered("test"))
    }
  }

  "A registered class" should "be encodable via encode" in {
    val encoding = KryoEncoding(Seq(classOf[Registered]))
    val testValue = new Registered(1l, "test")
    val res = encoding.decodeFromBytes[Registered](encoding.encodeToBytes(testValue), classOf[Registered])
    res should equal(testValue)
  }

  "A registered class" should "be encodable via encodeToBuffer" in {
    val encoding = KryoEncoding(Seq(classOf[Registered]))
    val testValue = new Registered(1l, "test")
    val buffer = encoding.encodeToBuffer(testValue)
    val mockMessage = mock[Message[Buffer]]
    when(mockMessage.body()).thenReturn(buffer)
    val res = encoding.decodeFromMessage[Registered](mockMessage)
    res.body should equal(testValue)
  }

  "A decoder-Handler" should "handle decoding" in {
    val encoding = KryoEncoding(Seq(classOf[Registered]))
    val testValue = new Registered(1l, "test")
    var receivedAndTestValueAreEqual = false
    val decodedHandler: Handler[KryoMessage[Registered]] =
      d => receivedAndTestValueAreEqual = testValue == d.body
    val decoderHandler = encoding.decoder(decodedHandler)

    val buffer = encoding.encodeToBuffer(testValue)
    val mockMessage = mock[Message[Buffer]]
    when(mockMessage.body()).thenReturn(buffer)
    decoderHandler.handle(mockMessage)

    receivedAndTestValueAreEqual should be (true)
  }

}

case class NotRegistered(name: String)

case class Registered(id:Long, name: String)
