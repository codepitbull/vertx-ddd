package io.vertx.scala.ddd.vertx

import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.ddd.VerticleTesting
import org.scalatest.Matchers

class EventSourcingVerticleSpec extends VerticleTesting[EventSourcingVerticle] with Matchers{

  import EventSourcingVerticle._

  "A message sent to the verticle" should "be persisted and read back" in {
    val appenderSender = vertx.eventBus().sender[Buffer](s"${AddressDefault}.${AddressAppend}")
    val replaySender = vertx.eventBus().sender[Long](s"${AddressDefault}.${AddressReplay}")
    val testBuffer = Buffer.buffer("hello world")
    appenderSender
      .sendFuture[Long](testBuffer)
      .flatMap(r => {
        println(r.body())
        replaySender.sendFuture[String](0)
            .flatMap(h => h.body() should equal("hello world"))
      })
  }

  override def config(): JsonObject = super.config().put(ConfigTemporary, true)
}
