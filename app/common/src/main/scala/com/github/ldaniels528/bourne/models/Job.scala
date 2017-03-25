package com.github.ldaniels528.bourne.models

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a job
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Job(val _id: String,
          val name: String,
          var input: String,
          var workflowConfig: String,
          var state: String = JobStates.NEW,
          var message: js.UndefOr[String] = js.undefined,
          var statistics: js.UndefOr[StatisticsLike] = js.undefined) extends js.Object

