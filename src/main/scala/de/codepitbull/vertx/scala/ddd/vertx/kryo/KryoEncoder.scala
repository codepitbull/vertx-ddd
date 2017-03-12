package de.codepitbull.vertx.scala.ddd.vertx.kryo

import java.io.ByteArrayOutputStream

import com.esotericsoftware.kryo.io.{Input, Output}
import com.twitter.chill.{Kryo, ScalaKryoInstantiator}

/**
  * Makes Kryo-encoding magically Thread-safe.
  */
class KryoEncoder {
  private val tl = new ThreadLocal[Tuple3[Kryo, Input, Output]] {
    override def initialValue(): Tuple3[Kryo, Input, Output] = {
      val kryo = new ScalaKryoInstantiator().newKryo()
      val output = new Output(new ByteArrayOutputStream)
      val input = new Input()
      (kryo, input, output)
    }
  }

  def decodeFromBytes(bytes: Array[Byte]): Object = {
    val k = tl.get()
    k._2.setBuffer(bytes)
    k._1.readClassAndObject(k._2)
  }

  def encodeToBytes(s: Object): Array[Byte] = {
    val k = tl.get()
    k._1.writeClassAndObject(k._3, s)
    val ret = k._3.toBytes
    k._3.clear()
    ret
  }

}

object KryoEncoder {
  def apply(): KryoEncoder = new KryoEncoder()
}