package com.github.ldaniels528.bourne.rest

import com.github.ldaniels528.bourne.models.{JobLike, JobStates, StatisticsLike}

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a job model
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Job(val _id: js.UndefOr[String] = js.undefined,
          val name: js.UndefOr[String],
          var input: js.UndefOr[String],
          var inputSize: js.UndefOr[Double],
          var state: js.UndefOr[String] = JobStates.NEW,
          var workflowName: js.UndefOr[String],
          var processingHost: js.UndefOr[String] = js.undefined,
          var slaveID: js.UndefOr[String] = js.undefined,
          var lastUpdated: js.UndefOr[Double] = js.Date.now(),
          var message: js.UndefOr[String] = js.undefined,
          var statistics: js.UndefOr[StatisticsLike] = js.undefined) extends JobLike

