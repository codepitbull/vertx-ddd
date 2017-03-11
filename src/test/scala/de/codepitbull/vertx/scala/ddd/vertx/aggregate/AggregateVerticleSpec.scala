package de.codepitbull.vertx.scala.ddd.vertx.aggregate

import de.codepitbull.vertx.scala.ddd.VerticleTesting
import de.codepitbull.vertx.scala.ddd.eventstore.EventStoreVerticle
import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.ScalaVerticle.nameForVerticle
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.core.DeploymentOptions
import de.codepitbull.vertx.scala.ddd.aggregates.TestAggregate
import de.codepitbull.vertx.scala.ddd.vertx.kryo.KryoEncoding
import org.scalatest.Matchers

import scala.concurrent.Await
import scala.concurrent.duration._

class AggregateVerticleSpec extends VerticleTesting[EventStoreVerticle] with Matchers {

  import EventStoreVerticle._

  override def config(): JsonObject = super.config().put(ConfigTemporary, true)

  "blaaa" should "bleee" in {
    fillStore()
    vertx.deployVerticleFuture(nameForVerticle[TestAggregateVerticle], DeploymentOptions().setConfig(config()))
      .flatMap(r => "1" should equal("1"))
  }

  def fillStore(): Unit = {
    val appenderSender = vertx.eventBus().sender[Buffer](s"${AddressDefault}.${AddressAppend}")
    val encoding = new KryoEncoding(Seq(classOf[TestAggregate]))
    val buffer = Buffer.buffer(encoding.encodeToBytes(TestAggregate(1l, "hello world 1")))
    Await.result(appenderSender.sendFuture[Long](buffer), 10 second)
    val buffer2 = Buffer.buffer(encoding.encodeToBytes(TestAggregate(2l, "hello world 2")))
    Await.result(appenderSender.sendFuture[Long](buffer2), 10 second)
  }

}