package io.vertx.scala.ddd.eventbus.kryo

import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.scala.core.eventbus.Message
import org.scalatest.{FlatSpec, Matchers}
import io.vertx.scala.ddd.kryo.KryoEncoding._
import io.vertx.scala.ddd.kryo.KryoMessage
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar

class KryoEncodingSpec extends FlatSpec with Matchers with MockitoSugar {

  "An unregistered class" should "not be encodable" in {
    assertThrows[IllegalArgumentException] {
      encodeToBuffer(new NotRegistered("test"))
    }
  }

  "A registered class" should "be encodable via encode" in {
    val testValue = new Registered("test")
    register(classOf[Registered])
    val res = decodeFromBytes[Registered](encodeToBytes(testValue), classOf[Registered])
    res should equal(testValue)
  }

  "A registered class" should "be encodable via encodeToBuffer" in {
    val testValue = new Registered("test")
    register(classOf[Registered])
    val buffer = encodeToBuffer(testValue)
    val mockMessage = mock[Message[Buffer]]
    when(mockMessage.body()).thenReturn(buffer)
    val res = decodeFromMessage[Registered](mockMessage)
    res.body should equal(testValue)
  }

  "A decoder-Handler" should "handle decoding" in {
    val testValue = new Registered("test")
    var receivedAndTestValueAreEqual = false
    register(classOf[Registered])
    val decodedHandler: Handler[KryoMessage[Registered]] =
      d => receivedAndTestValueAreEqual = testValue == d.body
    val decoderHandler = decoder(decodedHandler)

    val buffer = encodeToBuffer(testValue)
    val mockMessage = mock[Message[Buffer]]
    when(mockMessage.body()).thenReturn(buffer)
    decoderHandler.handle(mockMessage)

    receivedAndTestValueAreEqual should be (true)
  }

}

case class NotRegistered(name: String)

case class Registered(name: String)
