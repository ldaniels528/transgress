package com.github.ldaniels528.broadway.models

import java.util.UUID

import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a job
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Job(val id: String = UUID.randomUUID().toString,
          val name: String,
          val input: String,
          var status: String,
          var message: js.UndefOr[String] = js.undefined,
          var statistics: js.UndefOr[JobStatistics] = js.undefined) extends js.Object

/**
  * Job Companion
  * @author lawrence.daniels@gmail.com
  */
object Job {

  /**
    * Job Enrichment
    * @param job the given [[Job job]]
    */
  implicit class JobEnrichment(val job: Job) extends AnyVal {

    @inline
    def copy(id: js.UndefOr[String],
             name: js.UndefOr[String] = js.undefined,
             input: js.UndefOr[String] = js.undefined,
             status: js.UndefOr[String] = js.undefined,
             message: js.UndefOr[String] = js.undefined,
             statistics: js.UndefOr[JobStatistics] = js.undefined): Job = {
      new Job(
        id = id getOrElse job.id,
        name = name getOrElse job.name,
        input = input getOrElse job.input,
        status = status getOrElse job.status,
        message = message ?? job.message,
        statistics = statistics ?? job.statistics
      )
    }

  }


}

object JobStatuses {
  val STAGED = "STAGED"
  val QUEUED = "QUEUED"
  val RUNNING = "RUNNING"
  val FAILED = "FAILED"
  val SUCCESS = "SUCCESS"
}