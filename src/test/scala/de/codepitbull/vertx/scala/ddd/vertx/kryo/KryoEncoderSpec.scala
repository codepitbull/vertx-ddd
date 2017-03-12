package de.codepitbull.vertx.scala.ddd.vertx.kryo

import de.codepitbull.vertx.scala.ddd.vertx.kryo.KryoEncoder
import org.scalatest.{AsyncFlatSpec, Matchers}

class KryoEncoderSpec extends AsyncFlatSpec with Matchers {

  "Registering nom-case class" should "fail" in {
    assertThrows[IllegalArgumentException] {
      KryoEncoder(Seq(classOf[NonCaseClass]))
    }
  }

  "A case class" should "be (de)serializable using the raw decoder" in {
    val test = ACaseClass("12", Some(1))
    val codec = KryoEncoder(Seq(classOf[ACaseClass]))
    val encoded = codec.encodeToBytes(test)
    val decoded = codec.decodeFromBytes(encoded)
    test should equal(decoded)
  }

}




