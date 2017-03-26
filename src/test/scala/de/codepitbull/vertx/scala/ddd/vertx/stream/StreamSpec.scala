package de.codepitbull.vertx.scala.ddd.vertx.stream

import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.scala.core.Vertx
import io.vertx.scala.core.eventbus.Message
import org.scalatest.{Assertions, AsyncFlatSpec, Matchers}
import rx.lang.scala.Observable
import scala.reflect.runtime.universe._

import scala.concurrent.Promise

class StreamSpec extends AsyncFlatSpec with Matchers with Assertions {

  implicit val vertx = Vertx.vertx
  implicit val vertxExecutionContext = VertxExecutionContext(
    vertx.getOrCreateContext()
  )

  "A PartitionedEventBus" should "do its job" in {
    val prom = Promise[String]
    observe[String]("agg0")
      .foreach(a => prom.success(a.body()))

    vertx.eventBus().send("agg0", "hallo")
    prom.future.map(a => a should equal("hallo"))
  }

  def observe[T: TypeTag](address: String)(implicit vertx:Vertx): Observable[Message[T]] = {
    Observable[Message[T]](o => {
      vertx
        .eventBus()
        .consumer[T](address)
        .handler(h => o.onNext(h))
    })
  }
}
