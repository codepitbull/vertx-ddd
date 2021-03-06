package de.codepitbull.vertx.scala.ddd.vertx.aggregate

import de.codepitbull.vertx.scala.ddd.aggregates.EntitiesAddons._
import de.codepitbull.vertx.scala.ddd.aggregates.TestAggregate
import de.codepitbull.vertx.scala.ddd.vertx.aggregate.Persistence._
import de.codepitbull.vertx.scala.ext.kryo.KryoEncoder
import org.scalatest.{FlatSpec, Matchers}

class SnapshotStoragerSpec extends FlatSpec with Matchers {

  "A case class " should "be persistet and loaded correctly" in {
    val encoder = KryoEncoder()
    implicit val am = SnapshotStorage[TestAggregate]("test1", encoder, true)
    val testObject = TestAggregate(1l, "hallohalloallalal")
    testObject.persist
    am.retrieve(1l) should equal(Some(testObject))
  }


  "A case class " should "be persistet, modified and loaded correctly" in {
    val encoder = KryoEncoder()
    implicit val am = SnapshotStorage[TestAggregate]("test1", encoder, true)
    val testObject = TestAggregate(1l, "hallohalloallalal")
    testObject.persist
    am.retrieve(1l) should equal(Some(testObject))
    val testObject2 = TestAggregate(1l, "hallohalloallalalhhhhh")
    testObject2.persist
    am.retrieve(1l) should equal(Some(testObject2))
  }


  "Using the reserved offset-tracking-id" should "lead to an IllegalArgumentException" in {
    val encoder = KryoEncoder()
    implicit val am = SnapshotStorage[TestAggregate]("test1", encoder, true)
    val testObject = TestAggregate(SnapshotStorage.OffsetPositon, "hallohalloallalal")
    intercept[IllegalArgumentException](testObject.persist)
  }

}
