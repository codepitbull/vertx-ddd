package io.vertx.scala.ddd.vertx.eventstore

import io.vertx.scala.core.WorkerExecutor
import net.openhft.chronicle.queue.impl.single.{SingleChronicleQueue, SingleChronicleQueueBuilder}
import net.openhft.chronicle.queue.{ExcerptAppender, ExcerptTailer}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

class VertxEventStore(executor: WorkerExecutor, queue: SingleChronicleQueue, appender: ExcerptAppender, tailer: ExcerptTailer) {

  def read() = {
    executor.executeBlocking[String](
      () => {
        while (tailer.readDocument(r => println("READ " + r.read().text()))) {}
        "TTT"
      }, false)
  }

  def write(str: String) = {
    appender.writeDocument(r => r.write().text(str))
  }

}

object VertxEventStore {
  def apply(executor: WorkerExecutor, path: String)(implicit executionContext: ExecutionContext): Future[VertxEventStore] = {
    executor.executeBlocking[(SingleChronicleQueue, ExcerptAppender, ExcerptTailer)](() => {
      val queue = SingleChronicleQueueBuilder.binary(path).build()
      (queue, queue.acquireAppender(), queue.createTailer())
    }).flatMap(s => Future.successful(new VertxEventStore(executor, s._1, s._2, s._3)))
  }
}
