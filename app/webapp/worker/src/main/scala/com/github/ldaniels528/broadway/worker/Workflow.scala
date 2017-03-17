package com.github.ldaniels528.broadway.worker

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a Workflow
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Workflow(val input: String,
               val outputs: js.Array[String],
               val onError: Option[OnError],
               val sources: js.Array[Source]) extends js.Object

/**
  * Represents a Workflow Source
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Source(val name: String,
             val path: String,
             val `type`: String,
             val format: String,
             val columnHeaders: Boolean) extends js.Object