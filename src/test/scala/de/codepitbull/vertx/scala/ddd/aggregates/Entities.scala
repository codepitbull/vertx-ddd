package de.codepitbull.vertx.scala.ddd.aggregates

import de.codepitbull.vertx.scala.ddd.vertx.aggregate.Persistence.AggregateId

final case class TestAggregate(id: AggregateId, name: String)

final case class CreateAggregateCommand(id: AggregateId, name: String)

final case class AggregateCreatedEvent(id: AggregateId, name: String)

final case class UpdateAggregateCommand(id: AggregateId, newName: String)

final case class AggregateUpdatedEvent(id: AggregateId, newName: String)

final case class LoadAggregate(id: AggregateId)
