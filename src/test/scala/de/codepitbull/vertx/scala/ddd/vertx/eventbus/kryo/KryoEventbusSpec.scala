package de.codepitbull.vertx.scala.ddd.vertx.eventbus.kryo

import de.codepitbull.vertx.scala.ddd.vertx.kryo.KryoEncoding
import de.codepitbull.vertx.scala.ddd.vertx.kryo.KryoEncoding.CodecName
import io.vertx.core.eventbus.{EventBus => JEventBus}
import io.vertx.scala.core.Vertx
import io.vertx.scala.core.eventbus.DeliveryOptions
import org.scalatest.{AsyncFlatSpec, Matchers}

import scala.concurrent.Promise

class KryoEventbusSpec extends AsyncFlatSpec with Matchers {

  "Registering nom-case class" should "fail" in {
    val vertx = Vertx.vertx()
    assertThrows[IllegalArgumentException] {
      vertx.eventBus().asJava.asInstanceOf[JEventBus].registerCodec(new KryoEncoding(Seq(classOf[NonCaseClass])))
    }
  }

  "A case class" should "be (de)serializable directly" in {
    val test = ACaseClass("12", Some(1))
    val codec = new KryoEncoding(Seq(classOf[ACaseClass]))
    val encoded = codec.encodeToBytes(test)
    val decoded = codec.decodeFromBytes(encoded)
    test should equal(decoded)
  }

  "A case class" should "be (de)serializable over the eventbus" in {
    val test = ACaseClass("12", Some(1))
    val vertx = Vertx.vertx()
    val promise = Promise[AnyRef]
    vertx.eventBus().asJava.asInstanceOf[JEventBus].registerCodec(new KryoEncoding(Seq(classOf[ACaseClass])))
    vertx.eventBus().consumer[AnyRef]("testAddr")
      .handler(a => promise.success(a.body()))
    vertx.eventBus().sender("testAddr", DeliveryOptions().setCodecName(CodecName)).send(test)
    promise.future.flatMap(r => r should equal(test))
  }

}

case class ACaseClass(name: String, value: Option[Int])

class NonCaseClass(name: String, value: Option[Int])