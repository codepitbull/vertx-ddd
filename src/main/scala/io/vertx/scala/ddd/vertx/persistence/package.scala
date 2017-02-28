package io.vertx.scala.ddd

/**
  * Created by jochen on 28.02.17.
  */
package object persistence {
  type SerializedAggregate = Array[Byte]
  type AggregateId = Long
}
