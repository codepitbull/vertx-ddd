package de.codepitbull.vertx.scala.ddd.vertx.kryo

import java.io.ByteArrayOutputStream

import com.esotericsoftware.kryo.io.{Input, Output}
import com.twitter.chill.ScalaKryoInstantiator

class KryoEncoder {
  private val kryo = new ScalaKryoInstantiator().newKryo()

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
  def apply(): KryoEncoder = new KryoEncoder()
}