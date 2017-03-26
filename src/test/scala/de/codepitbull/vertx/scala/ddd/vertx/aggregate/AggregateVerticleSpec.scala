package de.codepitbull.vertx.scala.ddd.vertx.aggregate

import de.codepitbull.vertx.scala.ddd.VerticleTesting
import de.codepitbull.vertx.scala.ddd.aggregates.{AggregateCreatedEvent, CreateAggregateCommand, TestAggregate}
import de.codepitbull.vertx.scala.ext.kryo.KryoMessageCodec
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.core.eventbus.DeliveryOptions
import org.scalatest.{BeforeAndAfterAll, Matchers}

import scala.concurrent.Promise

class AggregateVerticleSpec extends VerticleTesting[TestAggregateVerticle] with BeforeAndAfterAll with Matchers {

  override protected def beforeAll(): Unit = {
    KryoMessageCodec().register(vertx.eventBus())
  }

  override def config(): JsonObject = super.config()
    .put(AggregateVerticle.ConfigPath, "")
    .put(AggregateVerticle.ConfigTemporary, true)

  "An AggregateCreatedEvent command" should "create a new aggregate" in {
    val promise = Promise[AggregateCreatedEvent]()

    val createSender = vertx.eventBus()
      .sender[AnyRef](TestAggregateVerticle.CommandAddress, DeliveryOptions()
      .setCodecName(KryoMessageCodec.CodecName))

    vertx.eventBus()
      .consumer[AggregateCreatedEvent](TestAggregateVerticle.PublishAddress)
      .handler(h => if(!promise.isCompleted) promise.success(h.body()))

    createSender.send(CreateAggregateCommand(1l, "hallo"))

    promise.future.flatMap(h => h should equal(AggregateCreatedEvent(1l, "hallo")))
  }

  "Retrieving a TestAggregate created via CreateAggregateCommand" should "succeed" in {
    val promise = Promise[TestAggregate]()

    val createSender = vertx.eventBus()
        .sender[AnyRef](TestAggregateVerticle.CommandAddress, DeliveryOptions()
        .setCodecName(KryoMessageCodec.CodecName))

    val viewSender = vertx.eventBus()
      .sender[Long](TestAggregateVerticle.ViewAddress)

    createSender.send[Boolean](CreateAggregateCommand(1l, "hallo"), _ =>
      viewSender.send[TestAggregate](1l, h => if(!promise.isCompleted) promise.success(h.result().body())))

    promise.future.flatMap(h => h should equal(TestAggregate(1l, "hallo")))
  }


  "Retrieving a TestAggregate not created via CreateAggregateCommand" should "fail" in {
    val promise = Promise[Boolean]()

    val createSender = vertx.eventBus()
      .sender[AnyRef](TestAggregateVerticle.CommandAddress, DeliveryOptions()
      .setCodecName(KryoMessageCodec.CodecName))

    val viewSender = vertx.eventBus()
      .sender[Long](TestAggregateVerticle.ViewAddress)

    createSender.send[Boolean](CreateAggregateCommand(1l, "hallo"), _ =>
      viewSender.send[TestAggregate](3l, h => if(!promise.isCompleted) promise.success(h.failed())))

    promise.future.flatMap(h => h should equal(true))
  }
}