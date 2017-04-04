package com.github.ldaniels528.transgress.watcher

import com.github.ldaniels528.transgress.LoggerFactory
import com.github.ldaniels528.transgress.rest.{Feed, FeedClient, Job, JobClient}
import com.github.ldaniels528.transgress.watcher.FeedProcessor._
import com.github.ldaniels528.transgress.watcher.models.Trigger
import io.scalajs.JSON
import io.scalajs.nodejs.fs.Fs
import io.scalajs.nodejs.path.Path
import io.scalajs.nodejs.setTimeout
import io.scalajs.npm.glob.Glob
import io.scalajs.util.DurationHelper._
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Feed Processor
  * @author lawrence.daniels@gmail.com
  */
class FeedProcessor(config: WatcherConfig)(implicit feedDAO: FeedClient, jobDAO: JobClient, ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val watching = js.Dictionary[Boolean]()

  def searchForNewFiles(): Unit = {
    for {
      (name, trigger) <- config.triggers getOrElse js.Dictionary()
      pattern <- trigger.patterns getOrElse js.Array()
    } {
      Glob.async(s"${config.incomingDirectory}/$pattern").future onComplete {
        case Success(files) =>
          for {
            file <- files if !isWatched(file)
          } {
            watch(file)
            ensureCompleteFile(trigger, file)
          }
        case Failure(e) =>
          logger.error(s"$name: Failed while searching for new files: ${e.getMessage}")
      }
    }
  }

  private def ensureCompleteFile(trigger: Trigger, file: String): Unit = {
    val outcome = for {
      stats <- Fs.statAsync(file).future
      rawFeed = new Feed(filename = file, size = stats.size, mtime = stats.mtime.getTime())
      feed <- feedDAO.upsertFeed(rawFeed)
    } yield (feed, stats)

    outcome onComplete {
      case Success((feed, stats)) =>
        //logger.info(s"feed => ${JSON.stringify(feed)}")
        feed match {
          case f if !f.size.contains(stats.size) | !f.mtime.contains(stats.mtime.getTime()) =>
            logger.info(s"${trigger.name}: '$file' has changed (size = ${stats.size - f.size.orZero}, mtime = ${stats.mtime.getTime() - f.mtime.orZero})")
            setTimeout(() => ensureCompleteFile(trigger, file), 5.seconds)
          case f if f.elapsedTime < 30.seconds =>
            logger.info(s"${trigger.name}: '$file' is still too young (${feed.elapsedTime} msec)")
            setTimeout(() => ensureCompleteFile(trigger, file), 5.seconds)
          case _ =>
            queueForProcessing(trigger, file, stats.size)
        }
      case Failure(e) =>
        logger.info(s"${trigger.name}: Failed to stat '$file'")
      // TODO retry up to N times?
    }
  }

  private def queueForProcessing(trigger: Trigger, file: String, fileSize: Double) = {
    for {
      triggerName <- trigger.name
      workFile <- config.workFile(file)
      workflowName <- trigger.workflowName
    } {
      logger.info(s"$triggerName: Queuing '$file' (workflow $workflowName)")
      val rawjob = new Job(name = file.baseFile(), input = file, inputSize = fileSize, workflowName = workflowName)
      val outcome = jobDAO.createJob(rawjob)

      outcome onComplete {
        case Success(Some(job)) =>
          logger.info(s"$triggerName: Created job ${job._id} (${job.workflowName}) for file '$file'...")
          logger.info(s"File '$workFile' is queued for processing...")
        //unwatch(file) // TODO mark completed?

        case Success(None) =>
          logger.error(s"Failed to create job: rawjob => ${JSON.stringify(rawjob)}")

        case Failure(e) =>
          logger.error(s"Failed to move '$file' to '${config.workDirectory}': ${e.getMessage}", e)
        // TODO retry up to N times?
      }
    }
  }

  private def isWatched(file: String): Boolean = watching.contains(file)

  private def watch(file: String) = watching(file) = true

  private def unwatch(file: String) = watching.remove(file)

}

/**
  * Feed Processor Companion
  * @author lawrence.daniels@gmail.com
  */
object FeedProcessor {

  /**
    * File Extensions
    * @param file the given filename
    */
  final implicit class FileExtensions(val file: String) extends AnyVal {

    @inline
    def baseFile(): js.UndefOr[String] = Path.parse(file).base

  }

}