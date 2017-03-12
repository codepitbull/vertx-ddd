package de.codepitbull.vertx.scala.ddd.vertx.kryo

import java.io.ByteArrayOutputStream

import com.esotericsoftware.kryo.io.{Input, Output}
import com.twitter.chill.ScalaKryoInstantiator

/**
  *
  */
class KryoEncoder(clazzes: Seq[Class[_]]) {
  validateCaseClasses(clazzes)
  private val kryo = new ScalaKryoInstantiator().setRegistrationRequired(true).newKryo()
  clazzes.foreach(kryo.register(_, kryo.getNextRegistrationId))

  val output = new Output(new ByteArrayOutputStream)
  val input = new Input()

  def decodeFromBytes(bytes: Array[Byte]): Object = {
    input.setBuffer(bytes)
    kryo.readClassAndObject(input)
  }

  def encodeToBytes(s: Object): Array[Byte] = {
    kryo.writeClassAndObject(output, s)
    val ret = output.toBytes
    output.clear()
    ret
  }

}

object KryoEncoder{
  def apply(clazzes: Seq[Class[_]]): KryoEncoder = new KryoEncoder(clazzes)
}