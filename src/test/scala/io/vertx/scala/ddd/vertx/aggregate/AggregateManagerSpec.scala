package io.vertx.scala.ddd.vertx.aggregate

import io.vertx.scala.core.Vertx
import io.vertx.scala.ddd.aggregates.TestAggregate
import io.vertx.scala.ddd.aggregates.EntitiesAddons._
import io.vertx.scala.ddd.vertx.kryo.KryoEncoding
import io.vertx.scala.ddd.vertx.aggregate.Persistence._
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class AggregateManagerSpec extends FlatSpec with Matchers{

  "A case class " should "be persistet and loaded correctly" in {
    val encoding = KryoEncoding(Seq(classOf[TestAggregate]))
    val vertx = Vertx.vertx()
    val executor = vertx.createSharedWorkerExecutor("test1")
    implicit val am = AggregateManager[TestAggregate]("test1", encoding, true)
    val testObject = TestAggregate(1l, "hallohalloallalal")
    testObject.persist
    am.retrieve(1l) should equal(testObject)
  }


  "A case class " should "be persistet, modified and loaded correctly" in {
    val encoding = KryoEncoding(Seq(classOf[TestAggregate]))
    val vertx = Vertx.vertx()
    val executor = vertx.createSharedWorkerExecutor("test1")
    implicit val am = AggregateManager[TestAggregate]("test1", encoding, true)
    val testObject = TestAggregate(1l, "hallohalloallalal")
    testObject.persist
    am.retrieve(1l) should equal(testObject)
    val testObject2 = TestAggregate(1l, "hallohalloallalalhhhhh")
    testObject2.persist
    am.retrieve(1l) should equal(testObject2)
  }


  "Using the reserved offset-tracking-id" should "lead to an IllegalArgumentException" in {
    val encoding = KryoEncoding(Seq(classOf[TestAggregate]))
    val vertx = Vertx.vertx()
    val executor = vertx.createSharedWorkerExecutor("test1")
    implicit val am = AggregateManager[TestAggregate]("test1", encoding, true)
    val testObject = TestAggregate(AggregateManager.OffsetPositon, "hallohalloallalal")
    intercept[IllegalArgumentException](testObject.persist)
  }

}
