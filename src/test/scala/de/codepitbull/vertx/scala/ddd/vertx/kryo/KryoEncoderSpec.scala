package de.codepitbull.vertx.scala.ddd.vertx.kryo

import org.scalatest.{AsyncFlatSpec, Matchers}

class KryoEncoderSpec extends AsyncFlatSpec with Matchers {

  "A case class" should "be (de)serializable using the raw decoder" in {
    val test = ACaseClass("12", Some(1))
    val codec = KryoEncoder()
    val encoded = codec.encodeToBytes(test)
    val decoded = codec.decodeFromBytes(encoded)
    test should equal(decoded)
  }

}




