package com.github.ldaniels528.broadway.worker

import com.github.ldaniels528.broadway.models.{Job, JobStatistics, JobStatuses}
import com.github.ldaniels528.broadway.rest.LoggerFactory
import io.scalajs.JSON
import io.scalajs.nodejs.fs.Fs
import io.scalajs.nodejs.setInterval
import io.scalajs.util.DurationHelper._
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Job Processor
  * @author lawrence.daniels@gmail.com
  */
class JobProcessor(config: WorkerConfig, jobs: js.Dictionary[Job])(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(getClass)
  private var active = 0

  /**
    * Executes the job processor
    */
  def execute(): Unit = {
    // if no process is active, start one ...
    if (active == 0) {
      jobs.find(_._2.status == JobStatuses.QUEUED) foreach { case (_, job) =>
        job.status = JobStatuses.RUNNING
        active += 1

        val options = ProcessingOptions(
          filename = job.input,
          collectionName = "listing_activity",
          useThrottling = false
        )

        val task = for {
          stats <- Fs.statAsync(options.filename)
          statsGen = new StatisticsGenerator(stats.size)
          _ = setInterval(() => update(job, statsGen), 5.seconds)
          totalInserted <- LoadListingActivity.run(options, statsGen)
        } yield totalInserted

        task onComplete {
          case Success(_) =>
            active -= 1
            job.status = JobStatuses.SUCCESS
            job.statistics = null
          case Failure(e) =>
            active -= 1
            logger.error(s"File '${job.input}' failed: ${e.getMessage}")
            job.status = JobStatuses.FAILED
            job.message = e.getMessage
        }
      }
    }
  }

  private def update(job: Job, statsGen: StatisticsGenerator): Unit = {
    statsGen.lastStats foreach { stats =>
      job.statistics = new JobStatistics(
        totalInserted = stats.totalInserted.toInt,
        bytesRead = stats.bytesRead.toInt,
        bytesPerSecond = stats.bytesPerSecond,
        recordsDelta = stats.recordsDelta.toInt,
        recordsPerSecond = stats.recordsPerSecond,
        pctComplete = stats.complete_%,
        completionTime = stats.completionTime
      )
      logger.info(s"job.statistics => ${JSON.stringify(job.statistics)}")
    }
  }

}
