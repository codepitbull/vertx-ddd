package de.codepitbull.vertx.scala.ddd.vertx.eventstore

import java.lang.Boolean.TRUE
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import de.codepitbull.vertx.scala.ddd.VerticleTesting
import de.codepitbull.vertx.scala.ext.kryo.KryoMessageCodec
import de.codepitbull.vertx.scala.ext.kryo.KryoMessageCodec.CodecName
import io.vertx.lang.scala.json.{Json, JsonObject}
import io.vertx.scala.core.eventbus.DeliveryOptions
import org.scalatest.Matchers

import scala.concurrent.Promise

class EventStoreVerticleSpec extends VerticleTesting[EventStoreVerticle] with Matchers {

  import EventStoreVerticle._

  "A message sent to the verticle" should "be persisted and read back" in {
    KryoMessageCodec().register(vertx.eventBus())
    val consumerAddress = UUID.randomUUID().toString
    val appenderSender = vertx.eventBus().sender[Object](s"${AddressDefault}.${AddressAppend}", DeliveryOptions().setCodecName(CodecName))
    val replaySender = vertx.eventBus().sender[JsonObject](s"${AddressDefault}.${AddressReplay}")

    val testEvent = TestEvent("hello world")


    val result = new AtomicReference[String]
    val promise = Promise[Unit]

    vertx
      .eventBus()
      .consumer[Object](consumerAddress)
      .handler(r => {
        r.body() match {
          case StreamFinish() => promise.success(())
          case TestEvent(s) => result.set(s)
        }
        r.reply(TRUE)
      })
      .completionFuture()
      .flatMap(r => {
        appenderSender
          .sendFuture[Long](testEvent)
          .map(r => {
            replaySender
              .send(Json.emptyObj().put("consumer", consumerAddress).put("offset", 0l))
          })
      })

    promise.future.flatMap(r => result.get() should equal("hello world"))
  }

  override def config(): JsonObject = super.config().put(ConfigTemporary, true)
}

case class TestEvent(hello: String)