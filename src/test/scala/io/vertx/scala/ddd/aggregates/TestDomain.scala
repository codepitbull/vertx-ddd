package io.vertx.scala.ddd.aggregates

import io.vertx.scala.ddd.persistence.AggregateId

final case class TestAggregate(id: AggregateId, name: String)
