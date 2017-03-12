package de.codepitbull.vertx.scala.ddd.vertx.aggregate

import java.util.UUID

import de.codepitbull.vertx.scala.ddd.vertx.eventstore.EventStoreVerticle._
import de.codepitbull.vertx.scala.ddd.vertx.kryo.KryoEncoder
import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.lang.scala.json.Json
import io.vertx.scala.core.eventbus.Message

import scala.concurrent.{Future, Promise}
import scala.reflect.runtime.universe._
import scala.util.{Failure, Success}

abstract class AggregateVerticle[T <: AnyRef : TypeTag] extends ScalaVerticle {

  var encoder: KryoEncoder = _

  override def startFuture(): Future[Unit] = {
    encoder = KryoEncoder(classes)
    val am = AggregateManager[T]("manager", encoder)
    val replayConsumerAddress = UUID.randomUUID().toString
    val replaySourceAddress = config.getString("replaySourceAddress", s"${AddressDefault}.${AddressReplay}")
    val replayStartMessage = Json.emptyObj().put("consumer", replayConsumerAddress).put("offset", am.lastOffset)
    val promise = Promise[Unit]()

    vertx.eventBus()
      .localConsumer[Buffer](replayConsumerAddress)
      .handler(handleIncomingReplayAndCompletePromiseOnEnd(promise) _)
      .completionFuture()
      .andThen {
        case Success(s) => vertx.eventBus().send(replaySourceAddress, replayStartMessage)
        case Failure(t) => promise.failure(t)
      }

    promise.future
  }

  def handleIncomingReplayAndCompletePromiseOnEnd(promise: Promise[Unit])(msg: Message[Buffer]): Unit = {
    if (msg.body().length() == 0)
      promise.success(())
    else
      println("IN " + encoder.decodeFromBytes(msg.body().getBytes) + " " + msg.body().getBytes.length)
  }

  def classes: Seq[Class[_]]
}
