package de.codepitbull.vertx.scala.ddd.vertx.aggregate

import java.util.UUID

import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.lang.scala.json.Json
import io.vertx.scala.core.eventbus.Message
import de.codepitbull.vertx.scala.ddd.eventstore.EventStoreVerticle._
import de.codepitbull.vertx.scala.ddd.vertx.kryo.KryoEncoding
import io.vertx.core.eventbus.{EventBus => JEventBus}

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}
import scala.reflect.runtime.universe._

abstract class AggregateVerticle[T <: AnyRef : TypeTag] extends ScalaVerticle {

  var encoding: KryoEncoding = _

  override def startFuture(): Future[Unit] = {
    encoding = new KryoEncoding(classes)
    vertx.eventBus().asJava.asInstanceOf[JEventBus].registerCodec(encoding)
    val am = AggregateManager[T]("manager", encoding)
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
      println("IN "+encoding.decodeFromBytes(msg.body().getBytes)+" "+msg.body().getBytes.length)
  }

  def classes: Seq[Class[_]]
}