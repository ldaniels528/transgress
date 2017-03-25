package com.github.ldaniels528.bourne.client

import com.github.ldaniels528.bourne.BytesHelper._

import scala.scalajs.js

/**
  * Bourne Filters
  * @author lawrence.daniels@gmail.com
  */
object Filters {
  private val timeUnits = Seq("sec", "min", "hour", "day", "month", "year")
  private val timeFactors = Seq(60, 60, 24, 30, 12)

  /**
    * Duration: Converts a given time stamp to a more human readable expression (e.g. "5 mins ago")
    */
  val duration: js.Function = () => { (time: js.UndefOr[Double]) => toDuration(time) }: js.Function

  val bytes: js.Function = () => { (aValue: js.UndefOr[Double]) => aValue.map(_.bytes) }: js.Function

  val bps: js.Function = () => { (aValue: js.UndefOr[Double]) => aValue.map(_.bps) }: js.Function

  /**
    * Converts the given time expression to a textual duration
    * @param aTime the given [[js.Date]] or time stamp (in milliseconds)
    * @return the duration (e.g. "10 mins ago")
    */
  def toDuration(aTime: js.UndefOr[Double]): js.UndefOr[String] = aTime map { time =>
    // compute the age
    var age = time
    var unit = 0
    while (unit < timeFactors.length && age >= timeFactors(unit)) {
      age /= timeFactors(unit)
      unit += 1
    }

    // make the age and unit names more readable
    val unitName = timeUnits(unit) + (if (age.toInt != 1) "s" else "")
    f"$age%.0f $unitName"
  }

}
