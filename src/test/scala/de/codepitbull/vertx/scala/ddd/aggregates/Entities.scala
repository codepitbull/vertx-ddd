package de.codepitbull.vertx.scala.ddd.aggregates

import de.codepitbull.vertx.scala.ddd.vertx.aggregate.Persistence.AggregateId

/**
  * Created by jochen on 28.02.17.
  */
final case class TestAggregate(id: AggregateId, name: String)
