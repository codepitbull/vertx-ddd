package io.vertx.scala.ddd.vertx.eventbus

import io.vertx.scala.core.Vertx
import io.vertx.scala.core.eventbus.{DeliveryOptions, Message}
import scala.reflect.runtime.universe._

import scala.util.matching.Regex

class PartitionedEventBus(vertx: Vertx, shardSize: Int) {

  val AddressPattern:Regex = "persistence.(\\w+).(\\d+)".r
  val AggregateId:String = "id"

  private val eventBus = vertx.eventBus()

  def send(address: String, message: AnyRef): PartitionedEventBus = {
    val addressAndHeader = extractAddressAndHeader(address)
    eventBus.send(addressAndHeader._1, message, DeliveryOptions().addHeader(AggregateId, addressAndHeader._2))
    this
  }

  def send(address: String, message: AnyRef, options: DeliveryOptions): PartitionedEventBus = {
    val addressAndHeader = extractAddressAndHeader(address)
    eventBus.send(addressAndHeader._1, message, options.addHeader("id", addressAndHeader._2))
    this
  }

  def sendFuture[T: TypeTag](address: String, message: AnyRef): scala.concurrent.Future[Message[T]] = {
    val addressAndHeader = extractAddressAndHeader(address)
    eventBus.sendFuture[T](addressAndHeader._1, message, DeliveryOptions().addHeader(AggregateId, addressAndHeader._2))
  }

  def sendFuture[T: TypeTag](address: String, message: AnyRef, options: DeliveryOptions): scala.concurrent.Future[Message[T]] = {
    val addressAndHeader = extractAddressAndHeader(address)
    eventBus.sendFuture[T](addressAndHeader._1, message, options.addHeader("id", addressAndHeader._2))
  }

  def extractAddressAndHeader(address: String): (String, String) = {
    address match {
      case AddressPattern(name, id) => (name+math.floor(id.toDouble / shardSize).toInt, id)
      case _ => throw new RuntimeException(s"Unable to match ${address}")
    }
  }
}

object PartitionedEventBus{
  def apply(vertx: Vertx, shardSize:Int = 100): PartitionedEventBus = new PartitionedEventBus(vertx, shardSize)
}