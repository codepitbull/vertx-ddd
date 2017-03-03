package io.vertx.scala.ddd.vertx

import io.vertx.core.buffer.Buffer
import io.vertx.scala.core.eventbus.Message
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.ddd.vertx.EventSourcingVerticle._
import io.vertx.scala.ddd.vertx.eventstore.EventStore

import scala.concurrent.Future
import scala.util.{Failure, Success}

object EventSourcingVerticle {
  val ConfigTemporary = "temporary"
  val TemporaryDefault = false
  val ConfigExecutorName = "executorName"
  val ExecutorNameDefault = "es_executor"
  val ConfigAddress = "eventstoreAddress"
  val AddressDefault = "es"
  val AddressReplay = "replay"
  val AddressAppend = "append"
}

class EventSourcingVerticle extends ScalaVerticle {
  override def startFuture(): Future[Unit] = {
    val executorName = config.getString(ConfigExecutorName, ExecutorNameDefault)
    val eventstoreAddress = config.getString(ConfigAddress, AddressDefault)
    val temporary = config.getBoolean(ConfigTemporary, TemporaryDefault)
    EventStore(vertx.createSharedWorkerExecutor(executorName), "name", 0l, temporary)
      .flatMap(es => {
        vertx.eventBus()
          .localConsumer[Long](s"${eventstoreAddress}.${AddressReplay}")
          .handler(handleReplay(es) _)
        vertx.eventBus()
          .localConsumer[Buffer](s"${eventstoreAddress}.${AddressAppend}")
          .handler(handleAppend(es) _)
        Future.successful(())
      })
  }

  def handleReplay(es: EventStore)(message: Message[Long]): Unit = {
    //TODO: should prevent starting replay twice
    es
      .moveReadIndexTo(message.body())
      .read()
      .andThen{
        case Success(r) => message.reply(Buffer.buffer(r._1.head))
        case Failure(t) => message.fail(0, "Unable to start replay")
      }

  }

  def handleAppend(es: EventStore)(message: Message[Buffer]): Unit = {
    message.reply(es.write(message.body().getBytes).asInstanceOf[AnyRef])
  }

}
