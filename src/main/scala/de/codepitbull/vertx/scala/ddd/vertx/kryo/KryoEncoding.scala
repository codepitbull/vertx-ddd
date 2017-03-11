package de.codepitbull.vertx.scala.ddd.vertx.kryo

import java.io.ByteArrayOutputStream

import com.esotericsoftware.kryo.io.{Input, Output}
import com.twitter.chill.ScalaKryoInstantiator
import de.codepitbull.vertx.scala.ddd.vertx.kryo.KryoEncoding.CodecName
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import io.vertx.scala.core.eventbus.EventBus
import io.vertx.core.eventbus.{EventBus => JEventBus}

/**
  * A codec implementation to allow case classes to be transfered via the [[io.vertx.scala.core.eventbus.EventBus]].
  * It is limited to ONLY support case classes as these are immutable.
  * It uses Twitter Chill under the hood so all Scala-types can be used.
  *
  * This class is NOT THREAD SAFE!
  */
class KryoEncoding(clazzes: Seq[Class[_]]) extends MessageCodec[Object, Object] {
  val nonCaseClasses = clazzes.filter(!isCaseClass(_))
  if (nonCaseClasses.nonEmpty)
    throw new IllegalArgumentException(s"The following classes aren't case classes: ${nonCaseClasses.mkString("/")}")
  private val kr = new ScalaKryoInstantiator
  private val kryo = kr.newKryo()
  kryo.setRegistrationRequired(true)

  clazzes.foreach(kryo.register(_, kryo.getNextRegistrationId))
  val output = new Output(new ByteArrayOutputStream)
  val input = new Input()

  def register(eventBus: EventBus):KryoEncoding = {
    eventBus.asJava.asInstanceOf[JEventBus].registerCodec(this)
    this
  }

  override def transform(s: Object): Object = s

  override def decodeFromWire(pos: Int, buffer: Buffer): Object = {
    decodeFromBytes(buffer.getBytes(0, buffer.length() - 1))
  }

  def decodeFromBytes(bytes: Array[Byte]): Object = {
    input.setBuffer(bytes)
    kryo.readClassAndObject(input)
  }

  override def name(): String = CodecName

  override def encodeToWire(buffer: Buffer, s: Object): Unit = {
    buffer.appendBytes(encodeToBytes(s))
  }

  def encodeToBytes(s: Object): Array[Byte] = {
    kryo.writeClassAndObject(output, s)
    val ret = output.toBytes
    output.clear()
    ret
  }

  override def systemCodecID(): Byte = -1


  def isCaseClass(v: Class[_]): Boolean = {
    import reflect.runtime.universe._
    runtimeMirror(v.getClass.getClassLoader).classSymbol(v).isCaseClass
  }

}

object KryoEncoding {
  val CodecName = "k"

  def apply(clazzes: Seq[Class[_]]): KryoEncoding = new KryoEncoding(clazzes)
}
