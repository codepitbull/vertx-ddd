package de.codepitbull.vertx.scala.ddd.vertx.kryo

import java.io.ByteArrayOutputStream

import com.esotericsoftware.kryo.io.{Input, Output}
import com.twitter.chill.{Kryo, ScalaKryoInstantiator}
import de.codepitbull.vertx.scala.ddd.vertx.kryo.KryoMessageCodec.CodecName
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import io.vertx.scala.core.eventbus.EventBus
import io.vertx.core.eventbus.{EventBus => JEventBus}

/**
  * A codec implementation to allow case classes to be transfered via the [[io.vertx.scala.core.eventbus.EventBus]].
  * It is limited to ONLY support case classes as these are immutable.
  * It uses Twitter Chill under the hood so all Scala-types can be used.
  *
  * This class uses ThreadLocals to protect the non-thread-safe Kryo-objects.
  */
class KryoMessageCodec(clazzes: Seq[Class[_]]) extends MessageCodec[Object, Object] {
  validateCaseClasses(clazzes)
  private val krTl = new ThreadLocal[KryoEncoder] {
    override def initialValue(): KryoEncoder = new KryoEncoder(clazzes)
  }

  private val output = new Output(new ByteArrayOutputStream)
  private val input = new Input()

  def register(eventBus: EventBus):KryoMessageCodec = {
    eventBus.asJava.asInstanceOf[JEventBus].registerCodec(this)
    this
  }

  override def transform(s: Object): Object = s

  override def decodeFromWire(pos: Int, buffer: Buffer): Object = {
    krTl.get().decodeFromBytes(buffer.getBytes(pos, buffer.length()))
  }

  override def name(): String = CodecName

  override def encodeToWire(buffer: Buffer, s: Object): Unit = {
    buffer.appendBytes(krTl.get().encodeToBytes(s))
  }

  override def systemCodecID(): Byte = -1

  def isCaseClass(v: Class[_]): Boolean = {
    import reflect.runtime.universe._
    runtimeMirror(v.getClass.getClassLoader).classSymbol(v).isCaseClass
  }

}

object KryoMessageCodec {
  val CodecName = "k"

  def apply(clazzes: Seq[Class[_]]): KryoMessageCodec = new KryoMessageCodec(clazzes)
}

