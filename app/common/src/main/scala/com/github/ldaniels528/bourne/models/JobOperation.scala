package com.github.ldaniels528.bourne.models

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a Job Operation
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class JobOperation(var jobId: js.UndefOr[String],
                   val paused: js.UndefOr[Boolean] = js.undefined,
                   val running: js.UndefOr[Boolean] = js.undefined,
                   val stopped: js.UndefOr[Boolean] = js.undefined,
                   val error: js.UndefOr[String] = js.undefined) extends js.Object {

  def isSuccess: Boolean = error.nonEmpty

  def isFailed: Boolean = error.nonEmpty

}