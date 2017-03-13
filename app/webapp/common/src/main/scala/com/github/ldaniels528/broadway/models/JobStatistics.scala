package com.github.ldaniels528.broadway.models

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a Statistics
  */
@ScalaJSDefined
class JobStatistics(val totalInserted: Int,
                    val bytesRead: Int,
                    val bytesPerSecond: Double,
                    val recordsDelta: Int,
                    val recordsPerSecond: Double,
                    val pctComplete: Double,
                    val completionTime: Double) extends js.Object