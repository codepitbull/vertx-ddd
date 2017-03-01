package io.vertx.scala.ddd.vertx.eventbus.kryo

import io.vertx.scala.core.Vertx
import io.vertx.scala.ddd.vertx.kryo._
import org.scalatest.{AsyncFlatSpec, Matchers}

class KryoEventbusSpec extends AsyncFlatSpec with Matchers {

  "A case class" should "be (de)serializable over the eventbus" in {
    implicit val encoding = KryoEncoding(Seq(classOf[TestIt2]))
    val test = TestIt2("12", Some(1))
    val vertx = Vertx.vertx()
    vertx.eventBus().consumerKryo[TestIt2]("testAddr", a => a.reply(a.body))
    vertx.eventBus().sendKryoFuture[TestIt2]("testAddr", test)
      .flatMap(r => r.body should equal(test))
  }

}

case class TestIt2(name: String, value: Option[Int])
