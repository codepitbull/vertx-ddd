package io.vertx.scala.ddd

import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.ddd.vertx.persistence.AggregateManager
import io.vertx.scala.ddd.vertx.eventstore.VertxEventStore

import scala.concurrent.Future
import scala.concurrent.Future._

class AggregateVerticle extends ScalaVerticle{

  override def start() = {

    vertx.sharedData().getClusterWideMapFuture[String,String]("hallo").onComplete(h => println(s"WOOHOO ${h.isFailure}"))

    val eventstore = VertxEventStore(vertx.createSharedWorkerExecutor("eventstore"), "./queue")

    val aggregateManager = AggregateManager(vertx.createSharedWorkerExecutor("manager"), "manager")
  }
}
