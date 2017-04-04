package com.github.ldaniels528.transgress.worker

import com.github.ldaniels528.transgress.BytesHelper._
import io.scalajs.npm.moment._
import io.scalajs.npm.moment.durationformat._

import scala.concurrent.duration._
import scala.scalajs.js

/**
  * ETL Statistics Generator
  * @param sourceFileSize  the size of the source file in bytes
  * @param updateFrequency the update frequency for statistics generation
  */
class StatisticsGenerator(var sourceFileSize: Double = 0, val updateFrequency: FiniteDuration = 5.seconds) {
  private var processStartTime = js.Date.now()
  private var bytesPerSecond = 0.0
  private var lastBytesRead = 0L
  private var lastRecordedCount = 0L
  private var lastUpdatedTime = js.Date.now()
  private var recordsPerSecond = 0.0

  var bytesRead = 0L
  var failures = 0L
  var totalRead = 0L
  var lastStats: Option[Statistics] = None

  /**
    * Sets the process start time (if delayed since creation of this instance)
    */
  def start(): Unit = processStartTime = js.Date.now()

  /**
    * Returns updated statistics every x frequency or when forced is true
    * @return an option of the [[Statistics]]
    */
  def update(): Statistics = {
    // compute the statistics
    val timeDelta = Math.max(js.Date.now() - lastUpdatedTime, 1.0)
    val timeDeltaSeconds = timeDelta / 1000
    lastUpdatedTime = js.Date.now()

    // records inserted
    val recordsDelta = totalRead - lastRecordedCount
    recordsPerSecond = recordsDelta / timeDeltaSeconds
    lastRecordedCount = totalRead

    // bytes read
    val bytesDelta = bytesRead - lastBytesRead
    bytesPerSecond = bytesDelta / timeDeltaSeconds
    lastBytesRead = bytesRead

    // estimated time of completion
    val complete_% = if (sourceFileSize == 0) 0 else (bytesRead / sourceFileSize) * 100.0
    val etc = {
      val avgBps = bytesRead / ((js.Date.now() - processStartTime) / 1000)
      (sourceFileSize - bytesRead) / avgBps
    }

    Statistics(
      totalInserted = totalRead,
      bytesRead = bytesRead,
      bytesPerSecond = bytesPerSecond,
      failures = failures,
      recordsDelta = recordsDelta,
      recordsPerSecond = recordsPerSecond,
      complete_%,
      completionTime = etc)
  }

}

/**
  * Represents a statistical snapshot of processing activity
  * @param totalInserted    the total number of records inserted
  * @param bytesRead        the current number of bytes retrieved
  * @param bytesPerSecond   the snapshot's bytes/second transfer rate
  * @param failures         the number of total failures
  * @param recordsDelta     the number of records inserted during this snapshot
  * @param recordsPerSecond the snapshot's records/second transfer rate
  * @param complete_%       the percentage of completion
  * @param completionTime   the estimated completion time
  */
case class Statistics(totalInserted: Long,
                      bytesRead: Long,
                      bytesPerSecond: Double,
                      failures: Long,
                      recordsDelta: Long,
                      recordsPerSecond: Double,
                      complete_% : Double,
                      completionTime: Double) {

  override def toString: String = {
    // generate the estimate complete time
    val etc = Moment.duration(completionTime, "seconds").format("h [hrs], m [min]")

    // return the statistics
    f"$totalInserted total (${complete_%}%.1f%% - $etc), failures $failures, $recordsDelta batch " +
      f"($recordsPerSecond%.1f records/sec, ${bytesPerSecond.bps})"
  }
}
