package de.codepitbull.vertx.scala.ddd.aggregates

import de.codepitbull.vertx.scala.ddd.vertx.aggregate.Persistence.{AggregateId, Persistent}



object EntitiesAddons {

  implicit object TestAggregatePersistence extends Persistent[TestAggregate] {
    override def id(x: TestAggregate): AggregateId = x.id
  }

}
