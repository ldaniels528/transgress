package com.github.ldaniels528.transgress.worker

import com.github.ldaniels528.transgress.worker.models.Statistics

import scala.scalajs.js

/**
  * ETL Statistics Generator
  * @param sourceFileSize the size of the source file in bytes
  */
class StatisticsGenerator(var sourceFileSize: Double = 0) {
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

    new Statistics(
      totalInserted = totalRead,
      bytesRead = bytesRead,
      bytesPerSecond = bytesPerSecond,
      failures = failures,
      recordsDelta = recordsDelta,
      recordsPerSecond = recordsPerSecond,
      pctComplete = complete_%,
      completionTime = etc)
  }

}
