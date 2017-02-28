package io.vertx.scala.ddd

import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.scala.core.eventbus.EventBus

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.runtime.universe._

package object kryo {

  /**
    * Pimp my library to allow the eventbus to work with Kryo.
    * @param eb
    */
  implicit class KryoBus(eb: EventBus) {

    /**
      * Creates a consumer that will decodeFromMessage incoming Messages containing a Buffer into the target class.
      * @param address the address to subscribe to
      * @param handler the handler receiving decoded messages
      * @tparam T deserialization target class
      */
    def consumerKryo[T:TypeTag](address: String, handler: Handler[KryoMessage[T]]): Unit = {
      eb.consumer[Buffer](address, decoder[T](handler))
    }

    /**
      * Creates a consumer that will decodeFromMessage incoming Messages containing a Buffer into the target class.
      * @param address the address to subscribe to
      * @param handler the handler receiving decoded messages
      * @tparam T deserialization target class
      * @return a future to wait on for cluster registration
      */
    def consumerKryoFuture[T:TypeTag](address: String, handler: Handler[KryoMessage[T]]): Future[Unit] = {
      eb.consumer[Buffer](address, decoder[T](handler)).completionFuture()
    }

    /**
      * Send a message to the given address and encode the payload to Kryo.
      * @param address target address
      * @param value the payload that should be encoded to Kryo (as a Buffer)
      */
    def sendKryo(address: String, value: AnyRef): Unit = {
      eb.send(address, encodeToBuffer(value))
    }

    /**
      * Send a message to the given address and encode the payload to Kryo.
      * @param address target address
      * @param value the payload that should be encoded to Kryo (as a Buffer)
      * @return a future to wait on for the reply
      */
    def sendKryoFuture[R:TypeTag](address: String, value: AnyRef)(implicit executionContext: ExecutionContext): Future[KryoMessage[R]] = {
      eb.sendFuture[Buffer](address, encodeToBuffer(value)).flatMap(r =>
        Future.successful(decodeFromMessage[R](r)))
    }
  }
}
