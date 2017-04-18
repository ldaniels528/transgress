package com.github.ldaniels528.transgress.worker.models

import com.github.ldaniels528.transgress.BytesHelper._
import io.scalajs.npm.moment.Moment
import io.scalajs.npm.moment.durationformat._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a statistical snapshot of processing activity
  * @param totalInserted    the total number of records inserted
  * @param bytesRead        the current number of bytes retrieved
  * @param bytesPerSecond   the snapshot's bytes/second transfer rate
  * @param failures         the number of total failures
  * @param recordsDelta     the number of records inserted during this snapshot
  * @param recordsPerSecond the snapshot's records/second transfer rate
  * @param pctComplete      the percentage of completion
  * @param completionTime   the estimated completion time
  */
@ScalaJSDefined
class Statistics(val totalInserted: Long,
                 val bytesRead: Long,
                 val bytesPerSecond: Double,
                 val failures: Long,
                 val recordsDelta: Long,
                 val recordsPerSecond: Double,
                 val pctComplete: Double,
                 val completionTime: Double) extends js.Object {

  override def toString: String = {
    // generate the estimate complete time
    val etc = Moment.duration(completionTime, "seconds").format("h [hrs], m [min]")

    // return the statistics
    f"$totalInserted total ($pctComplete%.1f%% - $etc), failures $failures, $recordsDelta batch " +
      f"($recordsPerSecond%.1f records/sec, ${bytesPerSecond.bps})"
  }

}