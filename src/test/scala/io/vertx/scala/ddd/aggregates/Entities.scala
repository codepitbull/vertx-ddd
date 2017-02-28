package io.vertx.scala.ddd.aggregates

import io.vertx.scala.ddd.vertx.persistence.Persistence.AggregateId

/**
  * Created by jochen on 28.02.17.
  */
final case class TestAggregate(id: AggregateId, name: String)
