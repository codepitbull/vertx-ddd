package io.vertx.scala.ddd

import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.ddd.vertx.eventstore.EventStore
import io.vertx.scala.ddd.vertx.kryo.KryoEncoding
import io.vertx.scala.ddd.vertx.persistence.AggregateManager

class AggregateVerticle extends ScalaVerticle {

  override def start() = {

    val encoding = KryoEncoding(Seq())
    val eventstore = EventStore(vertx.getOrCreateContext(),"./queue")

    val aggregateManager = AggregateManager(vertx.createSharedWorkerExecutor("manager"), "manager", encoding)


  }
}
