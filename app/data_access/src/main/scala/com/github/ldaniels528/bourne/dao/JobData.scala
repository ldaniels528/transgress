package com.github.ldaniels528.bourne
package dao

import com.github.ldaniels528.bourne.models.JobStatistics
import io.scalajs.npm.mongodb.ObjectID

import scala.scalajs.js

/**
  * Represents a job document
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait JobData extends js.Object {
  var _id: js.UndefOr[ObjectID] = js.native
  var name: js.UndefOr[String] = js.native
  var input: js.UndefOr[String] = js.native
  var workflowConfig: js.UndefOr[String] = js.native
  var state: js.UndefOr[String] = js.native
  var message: js.UndefOr[String] = js.native
  var statistics: js.UndefOr[JobStatistics] = js.native
}
