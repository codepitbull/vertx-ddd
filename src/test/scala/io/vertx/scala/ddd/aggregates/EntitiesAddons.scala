package io.vertx.scala.ddd.aggregates

import io.vertx.scala.ddd.vertx.persistence.Persistence.{AggregateId, Persistent}



object EntitiesAddons {

  implicit object TestAggregatePersistence extends Persistent[TestAggregate] {
    override def id(x: TestAggregate) = x.id
  }

}
