package io.vertx.scala.ddd.vertx.persistence

import io.vertx.scala.core.Vertx
import io.vertx.scala.ddd.aggregates.TestAggregate
import io.vertx.scala.ddd.aggregates.EntitiesAddons._
import io.vertx.scala.ddd.vertx.persistence.Persistence._
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class AggregateManagerSpec extends FlatSpec with Matchers{

  "A case class " should "be persistet and loaded correctly" in {
    val vertx = Vertx.vertx()
    val executor = vertx.createSharedWorkerExecutor("test1")
    implicit val am = Await.result(AggregateManager[TestAggregate](executor, "test1"), 10 seconds)
    val testObject = TestAggregate(1l, "hallohalloallalal")
    testObject.persist
    am.retrieve(1l) should equal(testObject)
  }

}
