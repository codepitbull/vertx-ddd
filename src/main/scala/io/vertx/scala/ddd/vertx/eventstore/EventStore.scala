package io.vertx.scala.ddd.vertx.eventstore

import java.nio.file.Files

import io.vertx.scala.core.WorkerExecutor
import net.openhft.chronicle.bytes.Bytes
import net.openhft.chronicle.queue.impl.single.{SingleChronicleQueue, SingleChronicleQueueBuilder}
import net.openhft.chronicle.queue.{ExcerptAppender, ExcerptTailer}

import scala.collection.immutable.Stream.continually
import scala.concurrent.{ExecutionContext, Future}

class EventStore(executor: WorkerExecutor, queue: SingleChronicleQueue, appender: ExcerptAppender, tailer: ExcerptTailer) {

  def read(): Future[(List[Array[Byte]], Long)] = {
    executor.executeBlocking[(List[Array[Byte]], Long)](
      () => {
        val resultList = continually(tailer.readText())
          .takeWhile(_ != null)
          .map(_.getBytes)
          .toList
        (resultList, tailer.index())
      })
  }

  def write(bytes: Array[Byte]): Long = {
    appender.writeBytes(Bytes.elasticByteBuffer(bytes.size).write(bytes))
    appender.lastIndexAppended()
  }

  def readOffset(): Long = tailer.index()

  //DANGEROUS: should never be called while reading is in progress as it happens on a different thread!
  def moveReadIndexTo(startIndex: Long): EventStore = {
    tailer.moveToIndex(startIndex)
    this
  }

  def writeOffset(): Long = appender.lastIndexAppended()
}

object EventStore {
  def apply(executor: WorkerExecutor, path: String, startIndex: Long, temporary: Boolean = false)(implicit executionContext: ExecutionContext): Future[EventStore] = {
    executor.executeBlocking[(SingleChronicleQueue, ExcerptAppender, ExcerptTailer)](() => {
      val queue = if (temporary) {
        SingleChronicleQueueBuilder.binary(Files.createTempDirectory(path)).build()
      }
      else {
        SingleChronicleQueueBuilder.binary(path).build()
      }

      val tailer = queue.createTailer()
      tailer.moveToIndex(startIndex)
      (queue, queue.acquireAppender(), tailer)
    }).flatMap(s => Future.successful(new EventStore(executor, s._1, s._2, s._3)))
  }
}
