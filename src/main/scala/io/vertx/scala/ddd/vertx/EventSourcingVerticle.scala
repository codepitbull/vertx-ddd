package io.vertx.scala.ddd.vertx

import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.core.eventbus.Message
import io.vertx.scala.core.streams.Pump.pump
import io.vertx.scala.ddd.vertx.EventSourcingVerticle._
import io.vertx.scala.ddd.vertx.eventstore.EventStore

import scala.concurrent.Future

object EventSourcingVerticle {
  val ConfigTemporary = "temporary"
  val TemporaryDefault = false
  val ConfigAddress = "eventstoreAddress"
  val AddressDefault = "es"
  val AddressReplay = "replay"
  val AddressAppend = "append"
}

class EventSourcingVerticle extends ScalaVerticle {
  override def startFuture(): Future[Unit] = {
    val eventstoreAddress = config.getString(ConfigAddress, AddressDefault)
    val temporary = config.getBoolean(ConfigTemporary, TemporaryDefault)
    val es = EventStore(vertx.getOrCreateContext(), "name", temporary)
    vertx.eventBus()
      .localConsumer[JsonObject](s"${eventstoreAddress}.${AddressReplay}")
      .handler(handleReplay(es) _)
    vertx.eventBus()
      .localConsumer[Buffer](s"${eventstoreAddress}.${AddressAppend}")
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
