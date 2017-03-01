package io.vertx.scala.ddd.vertx.kryo

import java.io.ByteArrayOutputStream

import com.esotericsoftware.kryo.io.{Input, Output}
import com.twitter.chill.ScalaKryoInstantiator
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.scala.core.eventbus.Message

import scala.reflect.runtime.universe._

/**
  * Manage all Kryo-Encoding related things. Especially registration of encodable classes.
  */
class KryoEncoding(clazzes: Seq[Class[_]]) {
  protected val classLoaderMirror = runtimeMirror(getClass.getClassLoader)
  private val kr = new ScalaKryoInstantiator
  private val kryo = kr.newKryo()
  kryo.setRegistrationRequired(true)

  clazzes.foreach(kryo.register(_, kryo.getNextRegistrationId))

  /**
    * Encode the given AnyRef and return the bytes as a n[[Array]]
    *
    * @param encodee object to be encoded
    * @return
    */
  def encodeToBytes(encodee: AnyRef): Array[Byte] = {
    val output = new Output(new ByteArrayOutputStream)
    kryo.writeObject(output, encodee)
    output.toBytes
  }

  /**
    * Encode the given AnyRef and return the bytes as a [[io.vertx.core.buffer.Buffer]]
    *
    * @param encodee object to be encoded
    * @return
    */
  def encodeToBuffer(encodee: AnyRef): Buffer = {
    Buffer.buffer(encodeToBytes(encodee))
  }

  def decodeFromBytes[T](decodee: Array[Byte], clazz: Class[_]): T = {
    kryo.readObject(new Input(decodee), clazz).asInstanceOf[T]
  }

  /**
    * Transform an incoming [[io.vertx.scala.core.eventbus.Message]] into a [[KryoMessage]]
    *
    * @param message the message to decodeFromMessage
    * @param tag     type tag, required to allow Kryo to figure out what class it should create
    * @tparam T the expected result of the decoding operation
    * @return the resulting KryoMessage containing the decoded value
    */
  def decodeFromMessage[T](message: Message[Buffer])(implicit tag: TypeTag[T]): KryoMessage[T] = {
    val clazz = classLoaderMirror.runtimeClass(typeOf[T])
    KryoMessage(decodeFromBytes[T](message.body().getBytes, clazz), message)
  }

  /**
    * Creates a handler to decodeFromMessage an incoming [[io.vertx.scala.core.eventbus.Message]]
    *
    * @param handler the Handler that should receive the decoded message
    * @param tag     type tag, required to allow Kryo to figure out what class it should create
    * @tparam T the expected result of the decoding operation
    * @return a new Handler to be used with the underlying eventbus
    */
  def decoder[T](handler: Handler[KryoMessage[T]])(implicit tag: TypeTag[T]): Handler[Message[Buffer]] = {
    val clazz = classLoaderMirror.runtimeClass(typeOf[T])
    message: Message[Buffer] =>
      handler.handle(KryoMessage(decodeFromBytes[T](message.body().getBytes, clazz), message))
  }
}

object KryoEncoding {
  def apply(clazzes: Seq[Class[_]]): KryoEncoding = new KryoEncoding(clazzes)
}
