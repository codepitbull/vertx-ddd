package io.vertx.scala.ddd.vertx.aggregate

import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.ScalaVerticle.nameForVerticle
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.core.DeploymentOptions
import io.vertx.scala.ddd.VerticleTesting
import io.vertx.scala.ddd.aggregates.TestAggregate
import io.vertx.scala.ddd.vertx.eventstore.EventStoreVerticle
import io.vertx.scala.ddd.vertx.kryo.KryoEncoding
import org.scalatest.Matchers

import scala.concurrent.Await
import scala.concurrent.duration._

class AggregateVerticleSpec extends VerticleTesting[EventStoreVerticle] with Matchers {

  import io.vertx.scala.ddd.vertx.eventstore.EventStoreVerticle._

  override def config(): JsonObject = super.config().put(ConfigTemporary, true)

  "blaaa" should "bleee" in {
    fillStore()
    vertx.deployVerticleFuture(nameForVerticle[TestAggregateVerticle], DeploymentOptions().setConfig(config()))
      .flatMap(r => "1" should equal("1"))
  }

  def fillStore(): Unit = {
    val appenderSender = vertx.eventBus().sender[Buffer](s"${AddressDefault}.${AddressAppend}")
    val encoding = KryoEncoding(Seq(classOf[TestAggregate]))
    val buffer = encoding.encodeToBuffer(TestAggregate(1l, "hello world 1"))
    println("A " + encoding.decodeFromBytes(buffer.getBytes, classOf[TestAggregate])+" "+buffer.getBytes.length)
    Await.result(appenderSender.sendFuture[Long](buffer), 10 second)
    val buffer2 = encoding.encodeToBuffer(TestAggregate(2l, "hello world 2"))
    println("B " + encoding.decodeFromBytes(buffer2.getBytes, classOf[TestAggregate])+" "+buffer.getBytes.length)
    Await.result(appenderSender.sendFuture[Long](buffer2), 10 second)
  }

}