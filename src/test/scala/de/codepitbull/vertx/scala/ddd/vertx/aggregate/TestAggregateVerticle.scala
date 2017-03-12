package de.codepitbull.vertx.scala.ddd.vertx.aggregate

import de.codepitbull.vertx.scala.ddd.aggregates.TestAggregate
import de.codepitbull.vertx.scala.ddd.vertx.kryo.KryoEncoder

class TestAggregateVerticle extends AggregateVerticle[TestAggregate]{
  override val encoder: KryoEncoder = KryoEncoder()
}
