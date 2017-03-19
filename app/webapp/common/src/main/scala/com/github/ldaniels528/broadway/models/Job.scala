package com.github.ldaniels528.broadway.models

import java.util.UUID

import com.github.ldaniels528.broadway.models.JobStates.JobState

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
          var workflowConfig: String,
          var state: JobState = JobStates.NEW,
          var message: js.UndefOr[String] = js.undefined,
          var statistics: js.UndefOr[JobStatistics] = js.undefined) extends js.Object

/**
  * Job States Enumeration
  * @author lawrence.daniels@gmail.com
  */
object JobStates extends Enumeration {
  type JobState = Value
  val NEW, QUEUED, RUNNING, STOPPED, SUCCESS = Value
}
