package com.github.ldaniels528.bourne.worker.util

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.scalajs.js
import scala.util.Try

/**
  * Loader Utilities
  * @author lawrence.daniels@gmail.com
  */
object LoaderUtilities {
  private val UnitNames = Seq("Bytes", "KB", "MB", "GB", "TB")

  /**
    * Byte Unit Enrichment
    * @param value the given byte size
    */
  implicit class ByteUnitEnrichment(val value: Double) extends AnyVal {

    @inline
    def bytes: String = units(value)

    @inline
    def bps: String = units(value) + "/sec"

    @tailrec
    private def units(value: Double, unit: Int = 0): String = {
      if (value >= 1000) units(value / 1024, unit + 1) else f"$value%.2f ${UnitNames(unit)}"
    }

  }

  implicit class FutureEnrichment[T](val task: Future[T]) extends AnyVal {

    @inline
    def onceCompleted(outcome: Try[T] => Any)(implicit ec: ExecutionContext): task.type = {
      task onComplete outcome
      task
    }
  }

  implicit class JsPromiseEnrichment[T](val promise: js.Promise[T]) extends AnyVal {

    @inline
    def onceCompleted(outcome: Try[T] => Any)(implicit ec: ExecutionContext): Future[T] = {
      val task = promise.toFuture
      task onComplete outcome
      task
    }
  }

  implicit class PromiseEnrichment[T](val promise: Promise[T]) extends AnyVal {

    @inline
    def onceCompleted(outcome: Try[T] => Any)(implicit ec: ExecutionContext): Future[T] = {
      val task = promise.future
      task onComplete outcome
      task
    }
  }

}
