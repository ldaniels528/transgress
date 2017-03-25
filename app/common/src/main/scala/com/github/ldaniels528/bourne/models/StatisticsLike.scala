package com.github.ldaniels528.bourne.models

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a Statistics
  */
@ScalaJSDefined
class StatisticsLike(val totalInserted: Int,
                     val bytesRead: Int,
                     val bytesPerSecond: Double,
                     val recordsDelta: Int,
                     val recordsPerSecond: Double,
                     val pctComplete: Double,
                     val completionTime: Double) extends js.Object