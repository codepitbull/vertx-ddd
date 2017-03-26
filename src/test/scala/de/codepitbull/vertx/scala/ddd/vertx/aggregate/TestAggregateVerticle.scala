package de.codepitbull.vertx.scala.ddd.vertx.aggregate

import java.lang.Boolean.TRUE

import de.codepitbull.vertx.scala.ddd.aggregates.{AggregateCreatedEvent, CreateAggregateCommand, TestAggregate}
import de.codepitbull.vertx.scala.ext.kryo.KryoMessageCodec
import io.vertx.scala.core.eventbus.DeliveryOptions

import scala.concurrent.Future

object TestAggregateVerticle {
  val CommandAddress = "commands"
  val PublishAddress = "events"
  val ViewAddress = "view"
}

class TestAggregateVerticle extends AggregateVerticle[TestAggregate] {
  override def activate(snapshotStorage: SnapshotStorage[TestAggregate], eventStore: EventStore): Future[Unit] = {
    val pub = vertx.eventBus().publisher[AnyRef](TestAggregateVerticle.PublishAddress, DeliveryOptions().setCodecName(KryoMessageCodec.CodecName))

    vertx.eventBus()
      .localConsumer[AnyRef](TestAggregateVerticle.CommandAddress)
      .handler(b => b.body match {
        case CreateAggregateCommand(id, name) =>
          val event = AggregateCreatedEvent(id, name)
          eventStore.write(event)
          snapshotStorage.persist(id, TestAggregate(id, name))
          pub.send(event)
          b.reply(TRUE)
      })

    vertx.eventBus()
      .localConsumer[Long](TestAggregateVerticle.ViewAddress)
      .handler(id => snapshotStorage.retrieve(id.body()) match {
        case Some(i) => id.reply(i, DeliveryOptions().setCodecName(KryoMessageCodec.CodecName))
        case None => id.fail(1, "No such aggregate")
       })

    Future.successful()
  }
}
