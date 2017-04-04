package com.github.ldaniels528.transgress.watcher

import com.github.ldaniels528.transgress.LoggerFactory
import com.github.ldaniels528.transgress.rest.{Job, JobClient}
import com.github.ldaniels528.transgress.watcher.FeedProcessor._
import com.github.ldaniels528.transgress.watcher.models.Trigger
import io.scalajs.JSON
import io.scalajs.nodejs.fs.{Fs, Stats}
import io.scalajs.nodejs.path.Path
import io.scalajs.nodejs.setTimeout
import io.scalajs.npm.glob.Glob
import io.scalajs.util.DateHelper._
import io.scalajs.util.DurationHelper._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.{Failure, Success}

/**
  * Feed Processor
  * @author lawrence.daniels@gmail.com
  */
class FeedProcessor(config: WatcherConfig)(implicit jobDAO: JobClient, ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val watching = js.Dictionary[FileWatch]()

  def searchForNewFiles(): Unit = {
    for {
      (name, trigger) <- config.triggers getOrElse js.Dictionary()
      pattern <- trigger.patterns getOrElse js.Array()
    } {
      Glob.async(s"${config.incomingDirectory}/$pattern").future onComplete {
        case Success(files) =>
          for {
            file <- files if !isWatched(file)
          } ensureCompleteFile(trigger, file)
        case Failure(e) =>
          logger.error(s"$name: Failed while searching for new files: ${e.getMessage}")
      }
    }
  }

  private def ensureCompleteFile(trigger: Trigger, file: String): Unit = {
    Fs.statAsync(file).future onComplete {
      case Success(stats) =>
        watch(file, stats) match {
          case w if w.stats.size != stats.size || w.stats.mtime.getTime() != stats.mtime.getTime() =>
            logger.info(s"${trigger.name}: '$file' has changed (size = ${stats.size - w.stats.size}, mtime = ${stats.mtime - w.stats.mtime})")
            w.stats = stats
            setTimeout(() => ensureCompleteFile(trigger, file), 15.seconds)
          case w if w.elapsedTime < 30.seconds =>
            logger.info(s"${trigger.name}: '$file' is still too young (${w.elapsedTime} msec)")
            setTimeout(() => ensureCompleteFile(trigger, file), 7.5.seconds)
          case _ =>
            queueForProcessing(trigger, file, stats.size)
        }
      case Failure(e) =>
        logger.info(s"${trigger.name}: Failed to stat '$file'")
      // TODO retry up to N times?
    }
  }

  private def isWatched(file: String): Boolean = watching.contains(file)

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
          untrackFeed(file)

        case Success(None) =>
          logger.error(s"Failed to create job: rawjob => ${JSON.stringify(rawjob)}")

        case Failure(e) =>
          logger.error(s"Failed to move '$file' to '${config.workDirectory}': ${e.getMessage}", e)
        // TODO retry up to N times?
      }
    }
  }

  private def watch(file: String, stats: Stats): FileWatch = {
    watching.getOrElseUpdate(file, new FileWatch(stats))
  }

  private def untrackFeed(file: String) = {
    // TODO persist to db
    //watching.remove(file)
  }

}

/**
  * Feed Processor Companion
  * @author lawrence.daniels@gmail.com
  */
object FeedProcessor {

  @ScalaJSDefined
  class FileWatch(var stats: Stats) extends js.Object {
    def elapsedTime: Double = js.Date.now() - stats.mtime.getTime()
  }

  /**
    * File Extensions
    * @param file the given filename
    */
  final implicit class FileExtensions(val file: String) extends AnyVal {

    @inline
    def baseFile(): js.UndefOr[String] = Path.parse(file).base

  }

}