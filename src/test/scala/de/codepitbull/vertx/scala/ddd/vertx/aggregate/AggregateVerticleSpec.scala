package de.codepitbull.vertx.scala.ddd.vertx.aggregate

import de.codepitbull.vertx.scala.ddd.VerticleTesting
import de.codepitbull.vertx.scala.ddd.aggregates.TestAggregate
import de.codepitbull.vertx.scala.ddd.vertx.eventstore.EventStoreVerticle
import de.codepitbull.vertx.scala.ext.kryo.KryoMessageCodec
import de.codepitbull.vertx.scala.ext.kryo.KryoMessageCodec.CodecName
import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.ScalaVerticle.nameForVerticle
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.core.DeploymentOptions
import io.vertx.scala.core.eventbus.DeliveryOptions
import org.scalatest.Matchers

import scala.concurrent.Await
import scala.concurrent.duration._

class AggregateVerticleSpec extends VerticleTesting[EventStoreVerticle] with Matchers {

  import EventStoreVerticle._

  override def config(): JsonObject = super.config().put(ConfigTemporary, true)

  "blaaa" should "bleee" in {
    KryoMessageCodec().register(vertx.eventBus())
    fillStore()
    vertx.deployVerticleFuture(nameForVerticle[TestAggregateVerticle], DeploymentOptions().setConfig(config()))
      .flatMap(r => "1" should equal("1"))
  }

  def fillStore(): Unit = {
    val appenderSender = vertx.eventBus().sender[Object](s"${AddressDefault}.${AddressAppend}",
      DeliveryOptions().setCodecName(CodecName))
    Await.result(appenderSender.sendFuture[Long](TestAggregate(1l, "hello world 1")), 10 second)
    Await.result(appenderSender.sendFuture[Long](TestAggregate(1l, "hello world 2")), 10 second)
  }

}