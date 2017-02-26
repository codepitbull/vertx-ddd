package io.vertx.scala

import scala.util.{Failure, Success, Try}

/**
  * Taken from https://www.phdata.io/try-with-resources-in-scala/
  */
package object ddd {
  def cleanly[A, B](resource: A)(cleanup: A => Unit)(doWork: A => B): Try[B] = {
    try {
      Success(doWork(resource))
    } catch {
      case e: Exception => Failure(e)
    }
    finally {
      try {
        if (resource != null) {
          cleanup(resource)
        }
      } catch {
        case e: Exception => println(e) // should be logged
      }
    }
  }
}
