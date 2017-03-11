package de.codepitbull.vertx.scala.ddd.eventstore;

import io.vertx.core.Context;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

/**
 * MOVED HERE TO MAKE IT POLYGLOT
 */
public class ChronicleEventStore {
  private final Context ctx;
  private final SingleChronicleQueue queue;
  private final ExcerptAppender appender;

  public ChronicleEventStore(Context ctx, String path, Boolean temporary) {
    Objects.requireNonNull(ctx, "ctx must not be null");
    Objects.requireNonNull(path, "path must not be null");
    Objects.requireNonNull(temporary, "temporary must not be null");
    if (temporary) {
      try {
        queue = SingleChronicleQueueBuilder.binary(Files.createTempDirectory(path)).build();
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }
    } else {
      queue = SingleChronicleQueueBuilder.binary(path).build();
    }
    appender = queue.acquireAppender();
    this.ctx = ctx;
  }

  public Long write(Buffer bytes) {
    appender.writeBytes(Bytes.elasticByteBuffer(bytes.length()).write(bytes.getBytes()));
    return appender.lastIndexAppended();
  }

  public ReadStream<Buffer> readStreamFrom(Long offset) {
    return new EventReadStream(ctx, queue, offset);
  }

  public WriteStream<Buffer> writeStream() {
    return new EventWriteStream(ctx, queue);
  }

  public void close() {
    queue.close();
  }

}