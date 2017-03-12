package de.codepitbull.vertx.scala.ddd.eventstore

import de.codepitbull.vertx.scala.ddd.eventstore.EventStoreVerticle._
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
    val eventstoreAddress = config.getString(ConfigAddress, AddressDefault)
    val temporary = config.getBoolean(ConfigTemporary, TemporaryDefault)
    val es = ChronicleEventStore(vertx.getOrCreateContext(), "name", temporary)
    vertx.eventBus()
      .localConsumer[JsonObject](s"${eventstoreAddress}.${AddressReplay}")
      .handler(handleReplay(es) _)
    vertx.eventBus()
      .localConsumer[Buffer](s"${eventstoreAddress}.${AddressAppend}")
      .handler(handleAppend(es) _)
    Future.successful(())
  }


  def handleReplay(es: ChronicleEventStore)(message: Message[JsonObject]): Unit = {
    val replayTarget = vertx.eventBus().sender[Buffer](message.body().getString("consumer"))
    val replaySource = es.readStreamFrom(message.body().getLong("offset"))
    //Send an empty buffer to signal the end of the stream
    replaySource.endHandler(u => replayTarget.send(Buffer.buffer(0)))
    pump(replaySource, replayTarget).start()
  }


  def handleAppend(es: ChronicleEventStore)(message: Message[Buffer]): Unit = {
    message.reply(es.write(message.body()).asInstanceOf[AnyRef])
  }

}

