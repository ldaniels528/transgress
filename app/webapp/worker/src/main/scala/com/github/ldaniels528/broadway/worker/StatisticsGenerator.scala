package com.github.ldaniels528.broadway.worker

import com.github.ldaniels528.broadway.worker.StatisticsGenerator._
import io.scalajs.npm.moment._
import io.scalajs.npm.moment.durationformat._

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.scalajs.js

/**
  * ETL Statistics Generator
  * @param sourceFileSize  the size of the source file in bytes
  * @param updateFrequency the update frequency for statistics generation
  */
class StatisticsGenerator(val sourceFileSize: Double,
                          val updateFrequency: FiniteDuration = 5.seconds) {
  private var processStartTime = js.Date.now()
  var bytesRead = 0L
  private var bytesPerSecond = 0.0
  private var lastBytesRead = 0L
  private var lastRecordedCount = 0L
  private var lastUpdatedTime = js.Date.now()
  private var recordsPerSecond = 0.0
  var totalInserted = 0L

  var lastStats: Option[Statistics] = None

  /**
    * Sets the process start time (if delayed since creation of this instance)
    */
  def start(): Unit = {
    processStartTime = js.Date.now()
  }

  /**
    * Returns updated statistics every x frequency or when forced is true
    * @param force indicates whether the statistics should be forced.
    * @return an option of the [[Statistics]]
    */
  def update(force: Boolean = false): Option[Statistics] = {
    val timeDelta = Math.max(js.Date.now() - lastUpdatedTime, 1.0)
    if (force || timeDelta >= updateFrequency.toMillis) {
      // compute the statistics
      val timeDeltaSeconds = timeDelta / 1000
      lastUpdatedTime = js.Date.now()

      // records inserted
      val recordsDelta = totalInserted - lastRecordedCount
      recordsPerSecond = recordsDelta / timeDeltaSeconds
      lastRecordedCount = totalInserted

      // bytes read
      val bytesDelta = bytesRead - lastBytesRead
      bytesPerSecond = bytesDelta / timeDeltaSeconds
      lastBytesRead = bytesRead

      // estimated time of completion
      val complete_% = (bytesRead / sourceFileSize) * 100.0
      val etc = {
        val avgBps = bytesRead / ((js.Date.now() - processStartTime) / 1000)
        (sourceFileSize - bytesRead) / avgBps
      }

      lastStats = Some(Statistics(
        totalInserted = totalInserted,
        bytesRead = bytesRead,
        bytesPerSecond = bytesPerSecond,
        recordsDelta = recordsDelta,
        recordsPerSecond = recordsPerSecond,
        complete_%,
        completionTime = etc))

      lastStats
    }
    else None
  }

}

/**
  * Statistics Generator Companion
  * @author lawrence.daniels@gmail.com
  */
object StatisticsGenerator {
  private val UnitNames = Seq("Bytes", "KB", "MB", "GB", "TB")

  /**
    * Byte Unit Enrichment
    * @param value the given byte size
    */
  implicit class BytesUnitEnrichment(val value: Double) extends AnyVal {

    @inline
    def bytes: String = units(value)

    @inline
    def bps: String = units(value) + "/sec"

    @tailrec
    private def units(value: Double, unit: Int = 0): String = {
      if (value >= 1000) units(value / 1024, unit + 1) else f"$value%.2f ${UnitNames(unit)}"
    }

  }

}

/**
  * Represents a statistical snapshot of processing activity
  * @param totalInserted    the total number of records inserted
  * @param bytesRead        the current number of bytes retrieved
  * @param bytesPerSecond   the snapshot's bytes/second transfer rate
  * @param recordsDelta     the number of records inserted during this snapshot
  * @param recordsPerSecond the snapshot's records/second transfer rate
  * @param complete_%       the percentage of completion
  * @param completionTime   the estimated completion time
  */
case class Statistics(totalInserted: Long,
                      bytesRead: Long,
                      bytesPerSecond: Double,
                      recordsDelta: Long,
                      recordsPerSecond: Double,
                      complete_% : Double,
                      completionTime: Double) {

  override def toString: String = {
    // generate the estimate complete time
    val etc = Moment.duration(completionTime, "seconds").format("h [hrs], m [min]")

    // return the statistics
    f"$totalInserted total (${complete_%}%.1f%% - $etc), $recordsDelta batch " +
      f"($recordsPerSecond%.1f records/sec, ${bytesPerSecond.bps})"
  }
}
