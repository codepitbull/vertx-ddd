package io.vertx.scala.ddd.vertx.persistence

import java.io.File

import io.vertx.scala.core.WorkerExecutor
import io.vertx.scala.ddd.vertx.kryo.KryoEncoding
import io.vertx.scala.ddd.vertx.kryo.KryoEncoding.{decodeFromBytes, encodeToBytes, register}
import io.vertx.scala.ddd.vertx.persistence.Persistence.{AggregateId, SerializedAggregate}
import net.openhft.chronicle.map.ChronicleMap

import scala.concurrent.Future
import scala.reflect.runtime.universe._

class AggregateManager[A <: AnyRef](val name: String, theMap: ChronicleMap[AggregateId, SerializedAggregate], clazz: Class[_]) {
  def persist(id: AggregateId, aggregate: A): Unit = theMap.put(id.asInstanceOf[java.lang.Long], encodeToBytes(aggregate))

  def retrieve(id: AggregateId): A = decodeFromBytes(theMap.get(id), clazz)

  def close(): Unit = theMap.close()
}

/**
  * Methods on the object have to threadsafe as they are shared across all verticles.
  */
object AggregateManager {
  private val classLoaderMirror = runtimeMirror(getClass.getClassLoader)

  def apply[A <: AnyRef : TypeTag](executor: WorkerExecutor, name: String)(implicit tag: TypeTag[A]): Future[AggregateManager[A]] = {
    val clazz = classLoaderMirror.runtimeClass(typeOf(tag))
    register(clazz)
    executor.executeBlocking[(AggregateManager[A])](() => {
      val theMap = ChronicleMap
        .of(classOf[AggregateId], classOf[SerializedAggregate])
        .name(name)
        .averageValue("HAHAHAHAHAHHAHAHAHAHAHA".getBytes) //yes, there are better averages but I am lazy
        .entries(50000)
        .createPersistedTo(new File(name))
      new AggregateManager[A](name, theMap, clazz)
    })
  }
}
