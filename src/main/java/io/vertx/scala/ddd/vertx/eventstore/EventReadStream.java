package io.vertx.scala.ddd.vertx.eventstore;

import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptTailer;

import java.util.Objects;

public class EventReadStream implements ReadStream<Buffer> {
  private final Context ctx;
  private final ChronicleQueue queue;
  private final Long offset;
  private volatile Handler<Buffer> handler;
  private volatile Handler<Throwable> exceptionHandler;
  private volatile Handler<Void> endHandler;
  private volatile boolean paused = false;
  private volatile TailThread thread;

  public EventReadStream(Context ctx, ChronicleQueue queue, Long offset) {
    Objects.requireNonNull(ctx, "ctx must not be null");
    Objects.requireNonNull(queue, "queue must not be null");
    Objects.requireNonNull(offset, "offset must not be null");
    this.ctx = ctx;
    this.queue = queue;
    this.offset = offset;

  }

  @Override
  public ReadStream<Buffer> exceptionHandler(Handler<Throwable> exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
    return this;
  }

  @Override
  public ReadStream<Buffer> handler(Handler<Buffer> handler) {
    if(this.handler != null)
      throw new IllegalStateException("Already started");
    this.handler = handler;
    thread = new TailThread();
    thread.start();
    return this;
  }

  @Override
  public ReadStream<Buffer> pause() {
    paused = true;
    return this;
  }

  @Override
  public ReadStream<Buffer> resume() {
    paused = false;
    thread.notify();
    return this;
  }

  @Override
  public ReadStream<Buffer> endHandler(Handler<Void> endHandler) {
    this.endHandler = endHandler;
    return this;
  }

  private class TailThread extends Thread {
    @Override
    public void run() {
      ExcerptTailer tailer = queue.createTailer();
      tailer.moveToIndex(offset);
      while(!interrupted()) {
        String txt = tailer.readText();
        if(txt != null) {
          ctx.runOnContext(r -> handler.handle(Buffer.buffer(txt)));
        }
        else if (endHandler != null){
          ctx.runOnContext(r -> endHandler.handle(null));
        }
        if(paused) {
          try {
            wait();
          } catch (InterruptedException e) {
            interrupt();
          }
        }
      }
    }
  }
}
