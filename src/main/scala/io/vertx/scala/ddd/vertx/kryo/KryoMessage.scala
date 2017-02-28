package io.vertx.scala.ddd.vertx.kryo

import io.vertx.core.buffer.Buffer
import io.vertx.scala.core.eventbus.Message

/**
  * A small abstraction to encaosulate the original message and allowing to reply to the sender.
  * @param body
  * @param original
  * @tparam T
  */
class KryoMessage[T](val body:T, private val original: Message[Buffer]) {
  /**
    * Send a reply to the sender of the original message. The payload will be encoded using Kryo.
    * @param message
    */
  def reply(message: AnyRef): Unit = {
    original.reply(KryoEncoding.encodeToBuffer(message))
  }
}

object KryoMessage{
  def apply[T](body:T, original: Message[Buffer]): KryoMessage[T] =
    new KryoMessage[T](body, original)

}
