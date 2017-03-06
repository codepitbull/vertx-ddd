package io.vertx.scala.ddd.vertx.aggregate

import io.vertx.scala.ddd.aggregates.TestAggregate

class TestAggregateVerticle extends AggregateVerticle[TestAggregate]{
  override def classes: Seq[Class[_]] = Seq(classOf[TestAggregate])
}
