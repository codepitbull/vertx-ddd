package io.vertx.scala.ddd.eventbus.kryo

import io.vertx.scala.core.Vertx
import org.scalatest.{AsyncFlatSpec, FlatSpec, Matchers}

class KryoEventbusSpec extends AsyncFlatSpec with Matchers {

  "A case class" should "be (de)serializable over the eventbus" in {
    KryoEncoding.register(classOf[TestIt2])
    val test = TestIt2("12", Some(1))
    val vertx = Vertx.vertx()
    vertx.eventBus().consumerKryo[TestIt2]("testAddr", a => a.reply(a.body))
    vertx.eventBus().sendKryoFuture[TestIt2]("testAddr", test)
      .flatMap(r => r.body should equal(test))
  }

}

case class TestIt2(name: String, value: Option[Int])
