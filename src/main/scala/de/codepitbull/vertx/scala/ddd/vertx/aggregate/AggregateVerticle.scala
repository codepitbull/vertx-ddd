package de.codepitbull.vertx.scala.ddd.vertx.aggregate

import java.util.Objects.requireNonNull

import de.codepitbull.vertx.scala.ddd.vertx.aggregate.AggregateVerticle._
import de.codepitbull.vertx.scala.ext.kryo.KryoEncoder
import io.vertx.lang.scala.ScalaVerticle

import scala.concurrent.{Future, Promise}
import scala.reflect.runtime.universe._

object AggregateVerticle{
  val ConfigTemporary = "temporary"
  val ConfigPath = "path"
}

/**
  * Base class for handling an aggregate inside a Verticle.
  * @param ev$1
  * @tparam T the Aggregate class this verticle will handle
  */
abstract class AggregateVerticle[T <: AnyRef : TypeTag] extends ScalaVerticle {

  final override def startFuture(): Future[Unit] = {
    val temporary = config.getBoolean(ConfigTemporary, false)
    val path = config.getString(ConfigPath)
    requireNonNull(path)
    val promise = Promise[Unit]()
    val encoder = KryoEncoder()
    val snapshotStorage = SnapshotStorage[T]("manager", encoder)
    val eventStore = EventStore(vertx.getOrCreateContext(), "", encoder, temporary)
    eventStore.replay(snapshotStorage.lastOffset, {
      case ReplayFinished => promise.success(())
      case _ => println("")
    })
    promise.future.flatMap(_ => activate(snapshotStorage, eventStore))
  }

  def activate(snapshotStorage: SnapshotStorage[T], eventStore: EventStore): Future[Unit]
}
