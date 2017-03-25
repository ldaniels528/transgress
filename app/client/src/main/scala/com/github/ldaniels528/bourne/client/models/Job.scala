package com.github.ldaniels528.bourne.client
package models

import com.github.ldaniels528.bourne.models.{JobLike, StatisticsLike}

import scala.scalajs.js

/**
  * Represents a job model
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait Job extends JobLike with Expandable {
  var _id: js.UndefOr[String] = js.native
  var name: js.UndefOr[String] = js.native
  var input: js.UndefOr[String] = js.native
  var workflowConfig: js.UndefOr[String] = js.native
  var state: js.UndefOr[String] = js.native
  var message: js.UndefOr[String] = js.native
  var processingHost: js.UndefOr[String] = js.native
  var statistics: js.UndefOr[StatisticsLike] = js.native

}