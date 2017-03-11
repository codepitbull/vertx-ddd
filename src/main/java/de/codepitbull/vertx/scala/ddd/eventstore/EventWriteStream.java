package de.codepitbull.vertx.scala.ddd.eventstore;

import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;

import java.util.Objects;

public class EventWriteStream implements WriteStream<Buffer>{
  private final Context ctx;
  private final ChronicleQueue queue;
  private final ExcerptAppender appender;

  private Handler<Buffer> handler;
  private Handler<Throwable> exceptionHandler;
  private Handler<Void> endHandler;

  public EventWriteStream(Context ctx, ChronicleQueue queue) {
    Objects.requireNonNull(ctx, "ctx must not be null");
    Objects.requireNonNull(queue, "queue must not be null");
    this.ctx = ctx;
    this.queue = queue;
    this.appender = queue.acquireAppender();
  }

  public long postion() {
    return appender.lastIndexAppended();
  }

  @Override
  public WriteStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
    return this;
  }

  @Override
  public WriteStream<Buffer> write(Buffer buffer) {
    appender.writeBytes(Bytes.elasticByteBuffer(buffer.length()).write(buffer.getBytes()));
    return this;
  }

  @Override
  public void end() {
  }

  @Override
  public WriteStream<Buffer> setWriteQueueMaxSize(int i) {
    return this;
  }

  @Override
  public boolean writeQueueFull() {
    return false;
  }

  @Override
  public WriteStream<Buffer> drainHandler(Handler<Void> handler) {
    return this;
  }

  @Override
  public void end(Buffer buffer) {
    write(buffer);
  }
}
