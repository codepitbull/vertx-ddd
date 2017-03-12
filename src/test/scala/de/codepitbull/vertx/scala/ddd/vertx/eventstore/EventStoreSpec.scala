package de.codepitbull.vertx.scala.ddd.vertx.eventstore

import io.vertx.core.buffer.Buffer
import io.vertx.core.buffer.Buffer.buffer
import io.vertx.scala.core.Vertx
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}

class EventStoreSpec extends FlatSpec with Matchers {
  "An array of bytes " should "be persistet and loaded back correctly" in {
    val vertx = Vertx.vertx()
    val ctx = vertx.getOrCreateContext()
    val testBuffer = buffer("helo world 666".getBytes)
    val es = ChronicleEventStore(ctx, "huhu", true)
    es.write(testBuffer)
    val promise = Promise[Buffer]
    es.readStreamFrom(0l).handler(b => if (!promise.isCompleted) promise.success(b))
    val resultBuffer = Await.result(promise.future, 10 seconds)
    resultBuffer should equal(testBuffer)
  }

  "Reading from an offset postion" should "work" in {
    val vertx = Vertx.vertx()
    val ctx = vertx.getOrCreateContext()
    val testBuffer = buffer("helo world 666".getBytes)
    val es = ChronicleEventStore(ctx, "huhu", true)
    es.write(buffer("helo world 1".getBytes))
    es.write(buffer("helo world 2".getBytes))
    es.write(buffer("helo world 3".getBytes))
    es.write(buffer("helo world 4".getBytes))
    es.write(buffer("helo world 5".getBytes))
    val theTarget = es.write(testBuffer)
    es.write(buffer("helo world 7".getBytes))
    es.write(buffer("helo world 8".getBytes))
    val promise = Promise[Buffer]
    es.readStreamFrom(theTarget).handler(b => if (!promise.isCompleted) promise.success(b))
    val resultBuffer = Await.result(promise.future, 10 seconds)
    resultBuffer should equal(testBuffer)
  }

}
