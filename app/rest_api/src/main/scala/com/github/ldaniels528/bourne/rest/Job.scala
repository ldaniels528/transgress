package com.github.ldaniels528.bourne.rest

import com.github.ldaniels528.bourne.models.StatisticsLike

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a job model
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Job(val _id: js.UndefOr[String],
          val name: js.UndefOr[String],
          var input: js.UndefOr[String],
          var workflowConfig: js.UndefOr[String],
          var state: js.UndefOr[String],
          var message: js.UndefOr[String] = js.undefined,
          var statistics: js.UndefOr[StatisticsLike] = js.undefined) extends js.Object

