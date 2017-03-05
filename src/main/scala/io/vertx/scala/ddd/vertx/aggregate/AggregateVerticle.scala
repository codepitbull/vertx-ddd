package io.vertx.scala.ddd.vertx.aggregate

import java.util.UUID

import io.vertx.lang.scala.ScalaVerticle
import io.vertx.lang.scala.json.Json
import io.vertx.scala.ddd.vertx.kryo.KryoEncoding

class AggregateVerticle extends ScalaVerticle {

  override def start() = {

    val replayConsumerAddress= UUID.randomUUID().toString

    val replayStartMessage = Json.emptyObj().put("consumer", replayConsumerAddress).put("offset", 0l)

    val encoding = KryoEncoding(Seq())

    val aggregateManager = AggregateManager("manager", encoding)


  }
}
