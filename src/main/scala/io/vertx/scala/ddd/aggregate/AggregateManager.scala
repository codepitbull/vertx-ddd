package io.vertx.scala.ddd.aggregate

import java.io.File

import io.vertx.scala.core.WorkerExecutor
import net.openhft.chronicle.map.ChronicleMap

import scala.concurrent.Future

class AggregateManager(theMap: ChronicleMap[java.lang.Long, Array[Byte]]) {
}

object AggregateManager{
  def apply(executor: WorkerExecutor, name: String): Future[AggregateManager] = {
    executor.executeBlocking[(AggregateManager)](() => {
      val theMap = ChronicleMap
        .of(classOf[java.lang.Long], classOf[Array[Byte]])
        .name(name)
        .averageValue("HAHAHAHAHAHHAHAHAHAHAHA".getBytes)
        .entries(50000)
        .createPersistedTo(new File(name))
      new AggregateManager(theMap)
    })
  }
}
