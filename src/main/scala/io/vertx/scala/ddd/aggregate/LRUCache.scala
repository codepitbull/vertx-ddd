package io.vertx.scala.ddd.aggregate

import java.util
import java.util.Map.Entry

/**
  * A very simple, non-threadsafe LRU-Cache.
  */
class LRUCache[A,B](private val cacheSize:Int) extends util.LinkedHashMap[A,B](16, 0.75f, true){
  override def removeEldestEntry(eldest: Entry[A, B]): Boolean = {
    size() >= cacheSize
  }
}
