package de.codepitbull.vertx.scala.ddd.vertx.eventstore

import de.codepitbull.vertx.scala.ddd.vertx.eventstore.EventStoreVerticle._
import de.codepitbull.vertx.scala.ddd.vertx.kryo.KryoEncoder
import de.codepitbull.vertx.scala.ddd.vertx.kryo.KryoMessageCodec.CodecName
import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.core.eventbus.{DeliveryOptions, Message}
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
    val encoder = KryoEncoder()
    val es = ChronicleEventStore(vertx.getOrCreateContext(), "name", encoder, temporary)
    vertx.eventBus()
      .localConsumer[JsonObject](s"${eventstoreAddress}.${AddressReplay}")
      .handler(handleReplay(es) _)
    vertx.eventBus()
      .localConsumer[Object](s"${eventstoreAddress}.${AddressAppend}")
      .handler(handleAppend(es) _)
    Future.successful(())
  }


  def handleReplay(es: ChronicleEventStore)(message: Message[JsonObject]): Unit = {
    val replayTarget = vertx.eventBus().sender[Object](message.body().getString("consumer"), DeliveryOptions().setCodecName(CodecName))
    val replaySource = es.readStreamFrom(message.body().getLong("offset"))
    //Send an empty buffer to signal the end of the stream
    replaySource.endHandler(u => replayTarget.send(StreamFinish()))
    pump(replaySource, replayTarget).start()
  }


  def handleAppend(es: ChronicleEventStore)(message: Message[Object]): Unit = {
    message.reply(es.write(message.body()).asInstanceOf[AnyRef], DeliveryOptions().setCodecName(CodecName))
  }
}

sealed trait EventStoreMessage

case class StreamFinish() extends EventStoreMessage

