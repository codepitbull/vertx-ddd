package io.vertx.scala.ddd.vertx.aggregate

object Persistence {

  type SerializedAggregate = Array[Byte]
  type AggregateId = java.lang.Long

  trait Persistent[A <: AnyRef] {
    def id(x: A):java.lang.Long
  }

  implicit class PersistentUtil[A <: AnyRef](x: A)(implicit aggregateManager: AggregateManager[A]) {
    def persist(implicit persistable: Persistent[A]): Unit = {
      aggregateManager.persist(persistable.id(x), x)
    }
  }
}
