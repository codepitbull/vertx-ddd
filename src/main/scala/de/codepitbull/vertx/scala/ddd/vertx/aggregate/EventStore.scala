package de.codepitbull.vertx.scala.ddd.vertx.aggregate

import java.nio.file.Files

import de.codepitbull.vertx.scala.ext.kryo.KryoEncoder
import io.vertx.core.Handler
import io.vertx.scala.core.Context
import net.openhft.chronicle.bytes.Bytes
import net.openhft.chronicle.queue.ChronicleQueue
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder

case object ReplayFinished

class EventStore(ctx: Context, path: String, encoder: KryoEncoder, temporary: Boolean) {

  private var queue = if (temporary)
    SingleChronicleQueueBuilder.binary(Files.createTempDirectory(path)).build
  else
    SingleChronicleQueueBuilder.binary(path).build
  private var appender = queue.acquireAppender

  def write(o: Object): Long = {
    appender.writeBytes(Bytes.elasticByteBuffer().write(encoder.encodeToBytes(o)))
    appender.lastIndexAppended
  }

  def replay(from: Long, replayHandler: Handler[Object]) = {
    new TailThread(ctx, queue, from, replayHandler, encoder).start()
  }

  def close() {
    queue.close()
  }

  private class TailThread(ctx: Context, queue: ChronicleQueue, offset: Long, handler: Handler[Object], encoder: KryoEncoder) extends Thread {
    override def run() {
      val tailer = queue.createTailer
      tailer.moveToIndex(offset)
      while (!isInterrupted) {
        val byteBufferBytes = Bytes.elasticByteBuffer
        val readBytes = tailer.readBytes(byteBufferBytes)
        if (readBytes) {
          val decoded = encoder.decodeFromBytes(byteBufferBytes.toByteArray)
          ctx.runOnContext(r => handler.handle(decoded))
        }
        else {
          ctx.runOnContext(r => handler.handle(ReplayFinished))
          interrupt()
        }
      }
    }
  }

}

object EventStore {
  def apply(ctx: Context, path: String, encoder: KryoEncoder, temporary: Boolean = true): EventStore = new EventStore(ctx, path, encoder, temporary)
}
