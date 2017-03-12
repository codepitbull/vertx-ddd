package de.codepitbull.vertx.scala.ddd.vertx.eventstore

import java.nio.file.Files
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

import de.codepitbull.vertx.scala.ddd.vertx.kryo.KryoEncoder
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.streams.{ReadStream => JReadStream, WriteStream => JWriteStream}
import io.vertx.scala.core.Context
import io.vertx.scala.core.streams.{ReadStream, WriteStream}
import net.openhft.chronicle.bytes.Bytes
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder
import net.openhft.chronicle.queue.{ChronicleQueue, ExcerptAppender}

class ChronicleEventStore(ctx: Context, path: String, temporary: Boolean) {
  val encoder = KryoEncoder()

  private var queue = if (temporary)
    SingleChronicleQueueBuilder.binary(Files.createTempDirectory(path)).build
  else
    SingleChronicleQueueBuilder.binary(path).build
  private var appender = queue.acquireAppender

  def write(o: Object): Long = {
    appender.writeBytes(Bytes.elasticByteBuffer().write(encoder.encodeToBytes(o)))
    appender.lastIndexAppended
  }

  def readStreamFrom(offset: Long): ReadStream[Object] = ReadStream(EventReadStream(ctx, queue, offset))

  def writeStream: WriteStream[Buffer] = WriteStream(EventWriteStream(ctx, queue))

  def close() {
    queue.close()
  }
}

object ChronicleEventStore {
  def apply(ctx: Context, path: String, temporary: Boolean = true): ChronicleEventStore = new ChronicleEventStore(ctx, path, temporary)
}

class EventReadStream(ctx: Context, queue: ChronicleQueue, offset: Long) extends JReadStream[Object] {
  private val exceptionHandler = new AtomicReference[Handler[Throwable]]
  private val endHandler = new AtomicReference[Handler[Void]]
  private val paused = new AtomicBoolean(false)
  private var thread: TailThread = _

  override def exceptionHandler(exceptionHandler: Handler[Throwable]): JReadStream[Object] = {
    this.exceptionHandler.set(exceptionHandler)
    this
  }

  override def handler(handler: Handler[Object]): JReadStream[Object] = {
    if (this.thread != null) throw new IllegalStateException("Already started")
    thread = new TailThread(handler)
    thread.start()
    this
  }

  def pause: JReadStream[Object] = {
    paused.set(true)
    this
  }

  def resume: JReadStream[Object] = {
    paused.set(false)
    thread.notify()
    this
  }

  override def endHandler(endHandler: Handler[Void]): JReadStream[Object] = {
    this.endHandler.set(endHandler)
    this
  }

  private class TailThread(handler: Handler[Object]) extends Thread {
    override def run() {
      val encoder = KryoEncoder()
      val tailer = queue.createTailer
      tailer.moveToIndex(offset)
      while (!isInterrupted) {
        val byteBufferBytes = Bytes.elasticByteBuffer
        val readBytes = tailer.readBytes(byteBufferBytes)
        if (readBytes) ctx.runOnContext(r => handler.handle(encoder.decodeFromBytes(byteBufferBytes.toByteArray)))
        else {
          val eh = endHandler.get()
          if (eh != null) ctx.runOnContext(r => eh.handle(null))
          interrupt()
        }

        if (paused.get()) try
          this.wait()
        catch {
          case e: InterruptedException => interrupt()
        }
      }
    }
  }

}

object EventReadStream {
  def apply(ctx: Context, queue: ChronicleQueue, offset: Long): EventReadStream =
    new EventReadStream(ctx, queue, offset)
}

class EventWriteStream(val ctx: Context, val queue: ChronicleQueue) extends JWriteStream[Buffer] {
  private val appender: ExcerptAppender = queue.acquireAppender
  private var handler: Handler[Buffer] = _
  private var exceptionHandler: Handler[Throwable] = _
  private var endHandler: Handler[Void] = _

  def postion: Long = appender.lastIndexAppended

  override def exceptionHandler(handler: Handler[Throwable]): JWriteStream[Buffer] = this

  override def write(buffer: Buffer): JWriteStream[Buffer] = {
    appender.writeBytes(Bytes.elasticByteBuffer(buffer.length).write(buffer.getBytes))
    this
  }

  override def end(): Unit = {}

  override def setWriteQueueMaxSize(i: Int): JWriteStream[Buffer] = this

  override def writeQueueFull: Boolean = false

  override def drainHandler(handler: Handler[Void]): JWriteStream[Buffer] = this

  override def end(buffer: Buffer): Unit = write(buffer)
}

object EventWriteStream {
  def apply(ctx: Context, queue: ChronicleQueue): EventWriteStream =
    new EventWriteStream(ctx, queue)
}