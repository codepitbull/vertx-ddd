package io.vertx.scala.ddd

import io.vertx.scala.ddd.vertx.persistence.AggregateManager
import io.vertx.scala.ddd.vertx.persistence.Persistence.Persistent

package object aggregates {

  implicit object TestAggregatePersistence extends Persistent[TestAggregate] {
    override def persist(testAggregate: TestAggregate)(implicit aggregateManager: AggregateManager[TestAggregate]): Unit = {
      aggregateManager
    }

    override def id(aggregate: TestAggregate) = aggregate.id
  }
}
