package com.github.ldaniels528.broadway.models

import java.util.UUID

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a job
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Job(val id: String = UUID.randomUUID().toString,
          val name: String,
          var input: String,
          var status: String,
          var workflowConfig: String,
          var message: js.UndefOr[String] = js.undefined,
          var statistics: js.UndefOr[JobStatistics] = js.undefined) extends js.Object

object JobStatuses {
  val STAGED = "STAGED"
  val QUEUED = "QUEUED"
  val RUNNING = "RUNNING"
  val FAILED = "FAILED"
  val SUCCESS = "SUCCESS"
}