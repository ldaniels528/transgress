package com.github.ldaniels528.bourne.models

import com.github.ldaniels528.bourne.models.JobStates.JobState

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a job-like model
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
trait JobLike extends js.Object {

  def name: js.UndefOr[String]

  def input: js.UndefOr[String]

  def inputSize: js.UndefOr[Double]

  def workflowName: js.UndefOr[String]

  def state: js.UndefOr[JobState]

  def lastUpdated: js.UndefOr[Double]

  def message: js.UndefOr[String]

  def processingHost: js.UndefOr[String]

  def statistics: js.UndefOr[StatisticsLike]

}

/**
  * Job States Enumeration
  * @author lawrence.daniels@gmail.com
  */
object JobStates {
  type JobState = String

  val NEW: JobState = "NEW"
  val CLAIMED: JobState = "CLAIMED"
  val QUEUED: JobState = "QUEUED"
  val RUNNING: JobState = "RUNNING"
  val PAUSED: JobState = "PAUSED"
  val STOPPED: JobState = "STOPPED"
  val SUCCESS: JobState = "SUCCESS"

  def values: Iterator[JobState] = Seq(NEW, QUEUED, RUNNING, STOPPED, SUCCESS).iterator

}
