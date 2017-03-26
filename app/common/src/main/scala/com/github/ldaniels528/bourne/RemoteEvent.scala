package com.github.ldaniels528.bourne

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a Remote Event
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class RemoteEvent(val action: js.UndefOr[String], val data: js.UndefOr[String]) extends js.Object

/**
  * RemoteEvent Singleton
  * @author lawrence.daniels@gmail.com
  */
object RemoteEvent {
  val JOB_UPDATE = "JOB_UPDATE"

}