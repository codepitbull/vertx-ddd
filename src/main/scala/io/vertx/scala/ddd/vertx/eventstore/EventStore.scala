package io.vertx.scala.ddd.vertx.eventstore

import io.vertx.core.buffer.Buffer
import io.vertx.core.{Context => JContext}
import io.vertx.scala.core.Context
import io.vertx.scala.core.streams.{ReadStream, WriteStream}

class EventStore(eventStore: ChronicleEventStore) {
  def  write(bytes: Buffer): Long = eventStore.write(bytes)

  def readStreamFrom(offset: Long): ReadStream[Buffer] = ReadStream[Buffer](eventStore.readStreamFrom(offset))

  def writeStream: WriteStream[Buffer] = WriteStream[Buffer](eventStore.writeStream())

  def close():Unit = eventStore.close()
}

object EventStore {
  def apply(ctx: Context, path: String, temporary: Boolean = false): EventStore =
    new EventStore(new ChronicleEventStore(ctx.asJava.asInstanceOf[JContext], path, temporary))
}
