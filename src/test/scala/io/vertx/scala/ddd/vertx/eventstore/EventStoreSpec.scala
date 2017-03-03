package io.vertx.scala.ddd.vertx.eventstore

import io.vertx.scala.core.Vertx
import org.scalatest.{AsyncFlatSpec, Matchers}

class EventStoreSpec extends AsyncFlatSpec with Matchers {
  "A case class " should "be persistet and loaded correctly" in {
    val vertx = Vertx.vertx()
    val es = EventStore(vertx.createSharedWorkerExecutor("test"), "huhu", 0l, true)
    es.flatMap(es => {
      es.write(Array[Byte](0, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 11))
      es.read()
    })
      .map(r => {
        r._1(0) should equal(Array[Byte](0, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 11))
      })
  }

  "Multiple writes" should "be read asynchronously" in {
    val vertx = Vertx.vertx()
    val es = EventStore(vertx.createSharedWorkerExecutor("test"), "huhu", 0l, true)
    es.flatMap(es => {
      es.write(Array[Byte](0, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 1))
      es.write(Array[Byte](1, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 2))
      es.write(Array[Byte](2, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 3))
      es.write(Array[Byte](3, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 4))
      es.write(Array[Byte](4, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 5))
      es.write(Array[Byte](5, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 6))
      es.write(Array[Byte](6, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 7))
      es.write(Array[Byte](7, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 8))
      es.write(Array[Byte](8, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 9))
      es.write(Array[Byte](9, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 10))
      es.write(Array[Byte](10, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 11))
      es.write(Array[Byte](11, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 12))
      es.read()
    })
      .map(r => {
        r._1(11) should equal(Array[Byte](11, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 12))
      })
  }

  "Multiple writes" should "be read asynchronously with an explicit offset" in {
    val vertx = Vertx.vertx()
    val es = EventStore(vertx.createSharedWorkerExecutor("test"), "huhu", 0l, true)
    es.flatMap(es => {
      es.write(Array[Byte](0, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 1))
      es.write(Array[Byte](1, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 2))
      es.write(Array[Byte](2, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 3))
      es.write(Array[Byte](3, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 4))
      es.write(Array[Byte](4, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 5))
      es.write(Array[Byte](5, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 6))
      es.write(Array[Byte](6, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 7))
      es.moveReadIndexTo(es.writeOffset())
      es.write(Array[Byte](7, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 8))
      es.write(Array[Byte](8, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 9))
      es.write(Array[Byte](9, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 10))
      es.write(Array[Byte](10, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 11))
      es.write(Array[Byte](11, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 12))
      es.read()
    })
      .map(r => {
        r._1(1) should equal(Array[Byte](7, 1, 0, 127, 13, 12, 0, 1, 0, 127, 13, 8))
      })
  }

}
