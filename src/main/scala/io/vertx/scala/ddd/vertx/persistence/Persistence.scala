package io.vertx.scala.ddd.vertx.persistence

import io.vertx.scala.ddd.persistence.AggregateId

object Persistence {
  trait Persistent[A <: AnyRef] {
    def persist(aggregate: A)(implicit aggregateManager: AggregateManager[A])
    def id(aggregate: A): AggregateId
  }

  implicit class PersistentUtil[A <: AnyRef](x: A)(implicit aggregateManager: AggregateManager[A]) {
    def persist(implicit persistable: Persistent[A]): Unit = {
      persistable.persist(x)
    }
  }
}
