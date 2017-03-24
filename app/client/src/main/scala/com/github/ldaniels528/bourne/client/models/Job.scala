package com.github.ldaniels528.bourne.client.models

import com.github.ldaniels528.bourne.models.JobStatistics

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a job model
  */
@ScalaJSDefined
class Job(val _id: js.UndefOr[String],
          val name: js.UndefOr[String],
          var input: js.UndefOr[String],
          var workflowConfig: js.UndefOr[String],
          var state: js.UndefOr[String],
          var message: js.UndefOr[String] = js.undefined,
          var statistics: js.UndefOr[JobStatistics] = js.undefined) extends js.Object