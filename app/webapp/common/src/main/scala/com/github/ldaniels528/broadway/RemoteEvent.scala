package com.github.ldaniels528.broadway

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a Remote Event
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class RemoteEvent(val action: js.UndefOr[String], val data: js.UndefOr[String]) extends js.Object