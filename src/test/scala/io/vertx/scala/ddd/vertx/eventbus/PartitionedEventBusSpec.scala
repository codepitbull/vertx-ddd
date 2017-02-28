package io.vertx.scala.ddd.eventbus

import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.scala.core.Vertx
import io.vertx.scala.core.eventbus.Message
import org.scalatest.{Assertions, FlatSpec, Matchers}

class PartitionedEventBusSpec extends FlatSpec with Matchers with Assertions {

  val vertx = Vertx.vertx
  implicit val vertxExecutionContext = VertxExecutionContext(
    vertx.getOrCreateContext()
  )

  "A PartitionedEventBus" should "do its job" in {
    val messagingBus = PartitionedEventBus(vertx)
    vertx.eventBus().consumer[String]("agg0", {h:Message[String] => println("1 "+h.body())})
    vertx.eventBus().consumer[String]("agg1", {h:Message[String] => println("2 "+h.body())})
    messagingBus.send("persistence.agg.10", "10")
    messagingBus.send("persistence.agg.110", "110")
  }

}
