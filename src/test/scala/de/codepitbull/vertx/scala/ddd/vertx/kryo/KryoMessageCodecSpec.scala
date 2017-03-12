package de.codepitbull.vertx.scala.ddd.vertx.kryo

import de.codepitbull.vertx.scala.ddd.vertx.kryo.KryoMessageCodec.CodecName
import io.vertx.core.buffer.Buffer
import io.vertx.scala.core.Vertx
import io.vertx.scala.core.eventbus.DeliveryOptions
import org.scalatest.{AsyncFlatSpec, Matchers}

import scala.concurrent.Promise

class KryoMessageCodecSpec extends AsyncFlatSpec with Matchers {

  "A case class" should "be (de)serializable directly" in {
    val test = ACaseClass("12", Some(1))
    val encoder = KryoEncoder()
    val codec = KryoMessageCodec(encoder)
    val encoded = Buffer.buffer()
    codec.encodeToWire(encoded, test)
    val decoded = codec.decodeFromWire(0, encoded)
    test should equal(decoded)
  }

  "A case class" should "be (de)serializable over the eventbus" in {
    val test = ACaseClass("12", Some(1))
    val vertx = Vertx.vertx()
    val promise = Promise[AnyRef]
    val encoder = KryoEncoder()
    KryoMessageCodec(encoder).register(vertx.eventBus())
    vertx.eventBus().consumer[ACaseClass]("testAddr")
      .handler(a => promise.success(a.body()))
    vertx.eventBus().sender("testAddr", DeliveryOptions().setCodecName(CodecName)).send(test)
    promise.future.flatMap(r => r should equal(test))
  }

}


