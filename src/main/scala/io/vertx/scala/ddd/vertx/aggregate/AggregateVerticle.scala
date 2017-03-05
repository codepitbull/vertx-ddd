package io.vertx.scala.ddd.vertx.aggregate

import java.util.UUID

import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.lang.scala.json.Json
import io.vertx.scala.ddd.vertx.eventstore.EventStoreVerticle._
import io.vertx.scala.ddd.vertx.kryo.KryoEncoding

class AggregateVerticle extends ScalaVerticle {

  override def start() = {
    val replaySource = config.getString("replaySourceAddress", s"${AddressDefault}.${AddressReplay}")

    val replayConsumerAddress = UUID.randomUUID().toString

    val replayConsumerControlAddress = s"${replayConsumerAddress}.done"

    vertx.eventBus()
      .localConsumer[Buffer](replayConsumerAddress)
      .handler(b => println(b))
      .completionFuture()

    vertx.eventBus()
      .localConsumer[Buffer](replayConsumerControlAddress)
      .handler(b => println(b))
      .completionFuture()

    val replayStartMessage = Json.emptyObj().put("consumer", replayConsumerAddress).put("offset", 0l)

    val encoding = KryoEncoding(Seq())

    val aggregateManager = AggregateManager("manager", encoding)


  }
}
