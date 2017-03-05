package io.vertx.scala.ddd.vertx.eventstore

import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.core.eventbus.Message
import io.vertx.scala.core.streams.Pump.pump

import scala.concurrent.Future

object EventStoreVerticle {
  val ConfigTemporary = "temporary"
  val TemporaryDefault = false
  val ConfigAddress = "eventstoreAddress"
  val AddressDefault = "es"
  val AddressReplay = "replay"
  val AddressAppend = "append"
}

class EventStoreVerticle extends ScalaVerticle {
  override def startFuture(): Future[Unit] = {
    val eventstoreAddress = config.getString(EventStoreVerticle.ConfigAddress, EventStoreVerticle.AddressDefault)
    val temporary = config.getBoolean(EventStoreVerticle.ConfigTemporary, EventStoreVerticle.TemporaryDefault)
    val es = EventStore(vertx.getOrCreateContext(), "name", temporary)
    vertx.eventBus()
      .localConsumer[JsonObject](s"${eventstoreAddress}.${EventStoreVerticle.AddressReplay}")
      .handler(handleReplay(es) _)
    vertx.eventBus()
      .localConsumer[Buffer](s"${eventstoreAddress}.${EventStoreVerticle.AddressAppend}")
      .handler(handleAppend(es) _)
    Future.successful(())
  }


  def handleReplay(es: EventStore)(message: Message[JsonObject]): Unit = {
    val replaySource = es.readStreamFrom(message.body().getLong("offset"))
    val replayTarget = vertx.eventBus().sender[Buffer](message.body().getString("consumer"))
    pump(replaySource, replayTarget).start()
  }


  def handleAppend(es: EventStore)(message: Message[Buffer]): Unit = {
    message.reply(es.write(message.body()).asInstanceOf[AnyRef])
  }

}
