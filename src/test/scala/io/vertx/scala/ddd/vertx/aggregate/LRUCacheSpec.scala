package io.vertx.scala.ddd.vertx.aggregate

import org.scalatest.{FlatSpec, Matchers}

class LRUCacheSpec extends FlatSpec with Matchers{

  "The LRUCache" should "behave like a LRUCache" in {
    val cache = new LRUCache[Long, String](3)
    cache.put(1l, "a")
    cache.put(2l, "b")
    cache.put(3l, "c")
    cache.get(1l)
    cache.put(4l, "d")
    cache.get(2l) should be(null)
    cache.get(1l) should equal("a")
  }

}
