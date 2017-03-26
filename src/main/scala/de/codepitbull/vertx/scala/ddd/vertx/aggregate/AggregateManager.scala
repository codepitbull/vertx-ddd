package de.codepitbull.vertx.scala.ddd.vertx.aggregate

import java.io.File
import java.nio.ByteBuffer

import de.codepitbull.vertx.scala.ddd.vertx.aggregate.AggregateManager.OffsetPositon
import de.codepitbull.vertx.scala.ddd.vertx.aggregate.Persistence.{AggregateId, SerializedAggregate}
import de.codepitbull.vertx.scala.ext.kryo.KryoEncoder
import net.openhft.chronicle.map.ChronicleMap

import scala.reflect.runtime.universe._

class AggregateManager[A <: AnyRef](val name: String, theMap: ChronicleMap[AggregateId, SerializedAggregate], clazz: Class[_], encoder: KryoEncoder) {
  def persist(id: AggregateId, aggregate: A): Unit = {
    if (id != OffsetPositon)
      theMap.put(id.asInstanceOf[java.lang.Long], encoder.encodeToBytes(aggregate))
    else
      throw new IllegalArgumentException(s"An id value of ($id) is not allowed as it is used for offset-tracking!")
  }

  def retrieve(id: AggregateId): A = encoder.decodeFromBytes(theMap.get(id)).asInstanceOf[A]

  def close(): Unit = theMap.close()

  def markLastOffset(offset: Long): Long = {
    val bb = ByteBuffer.allocate(8)
    bb.putLong(offset)
    theMap.put(OffsetPositon, bb.array())
    offset
  }

  def lastOffset: Long = {
    val bb = ByteBuffer.allocate(8)
    bb.put(Option(theMap.get(OffsetPositon)).getOrElse(Array[Byte]()))
    bb.getLong()
  }
}

/**
  * Methods on the object have to threadsafe as they are shared across all verticles.
  */
object AggregateManager {
  val OffsetPositon: java.lang.Long = -1l
  private val classLoaderMirror = runtimeMirror(getClass.getClassLoader)

  def apply[A <: AnyRef : TypeTag](name: String, encoder: KryoEncoder, temporary: Boolean = false)(implicit tag: TypeTag[A]): AggregateManager[A] = {
    val clazz = classLoaderMirror.runtimeClass(typeOf(tag))
    val theMap = ChronicleMap
      .of(classOf[AggregateId], classOf[SerializedAggregate])
      .name(name)
      .averageValue("HAHAHAHAHAHHAHAHAHAHAHA".getBytes) //yes, there are better averages but I am lazy
      .entries(50000)
      .createPersistedTo(if (temporary) File.createTempFile(name, ".tmp") else new File(name))
    new AggregateManager[A](name, theMap, clazz, encoder)
  }
}
