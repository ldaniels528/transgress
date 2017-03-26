package com.github.ldaniels528.bourne
package dao

import com.github.ldaniels528.bourne.models.{JobLike, StatisticsLike}
import io.scalajs.npm.mongodb.ObjectID

import scala.scalajs.js

/**
  * Represents a job document
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait JobData extends JobLike {
  var _id: js.UndefOr[ObjectID] = js.native
  var name: js.UndefOr[String] = js.native
  var input: js.UndefOr[String] = js.native
  var inputSize: js.UndefOr[Double] = js.native
  var workflowName: js.UndefOr[String] = js.native
  var state: js.UndefOr[String] = js.native
  var lastUpdated: js.UndefOr[Double] = js.native
  var message: js.UndefOr[String] = js.native
  var processingHost: js.UndefOr[String] = js.native
  var statistics: js.UndefOr[StatisticsLike] = js.native
}
