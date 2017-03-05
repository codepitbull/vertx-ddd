package io.vertx.scala.ddd.vertx.eventstore

import java.lang.Boolean.TRUE
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.json.{Json, JsonObject}
import io.vertx.scala.ddd.VerticleTesting
import org.scalatest.Matchers

import scala.concurrent.Promise

class EventStoreVerticleSpec extends VerticleTesting[EventStoreVerticle] with Matchers{

  import io.vertx.scala.ddd.vertx.eventstore.EventStoreVerticle._

  "A message sent to the verticle" should "be persisted and read back" in {
    val consumerAddress= UUID.randomUUID().toString
    val appenderSender = vertx.eventBus().sender[Buffer](s"${AddressDefault}.${AddressAppend}")
    val replaySender = vertx.eventBus().sender[JsonObject](s"${AddressDefault}.${AddressReplay}")

    val testBuffer = Buffer.buffer("hello world")


    val result = new AtomicReference[String]
    val promise = Promise[Unit]

    vertx
      .eventBus()
      .consumer[Buffer](consumerAddress)
      .handler(r => {
        if(r.body().length() > 0) {
          result.set(r.body().toString)
        } else {
          promise.success(())
        }
        r.reply(TRUE)
      })
      .completionFuture()
      .flatMap(r => {
        appenderSender
          .sendFuture[Long](testBuffer)
          .map(r => {
            replaySender
              .send(Json.emptyObj().put("consumer", consumerAddress).put("offset", 0l))
          })
      })

    promise.future.flatMap(r => result.get() should equal("hello world"))
  }

  override def config(): JsonObject = super.config().put(ConfigTemporary, true)
}
