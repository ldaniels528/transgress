package com.github.ldaniels528.bourne.worker

import com.github.ldaniels528.bourne.rest.Job
import com.github.ldaniels528.bourne.worker.JobFileTracking.FileWatch
import io.scalajs.nodejs.fs.Stats
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.Future
import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Job File Tracking
  * @author lawrence.daniels@gmail.com
  */
trait JobFileTracking {
  private val jobs = js.Dictionary[Job]()
  private val eventHandlers = js.Dictionary[JobEventHandler]()
  private val watching = js.Dictionary[FileWatch]()

  def isTracked(file: String): Boolean = jobs.contains(file)

  def isWatched(file: String): Boolean = watching.contains(file)

  def isWatchedOrTracked(file: String): Boolean = isTracked(file) || isWatched(file)

  def lookupJob(file: String): Option[Job] = jobs.get(file)

  def registerEventHandler(job: Job, handler: JobEventHandler): JobEventHandler = {
    eventHandlers(job._id.orNull) = handler
    handler
  }

  def pauseJob(job: Job): Future[Boolean] = {
    val result = for {
      _id <- job._id.toOption
      handler <- eventHandlers.get(_id)
    } yield handler

    result match {
      case Some(handler) => handler.pause()
      case None => Future.failed(js.JavaScriptException(s"No handler found for job ${job._id}"))
    }
  }

  def resumeJob(job: Job): Future[Boolean] = {
    val result = for {
      _id <- job._id.toOption
      handler <- eventHandlers.get(_id)
    } yield handler

    result match {
      case Some(handler) => handler.resume()
      case None => Future.failed(js.JavaScriptException(s"No handler found for job ${job._id}"))
    }
  }

  def stopJob(job: Job): Future[Boolean] = {
    val result = for {
      _id <- job._id.toOption
      handler <- eventHandlers.get(_id)
    } yield handler

    result match {
      case Some(handler) => handler.stop()
      case None => Future.failed(js.JavaScriptException(s"No handler found for job ${job._id}"))
    }
  }

  def trackJob(file: String, job: Job): Unit = {
    jobs(file) = job
    watching.remove(file)
  }

  def untrackJob(job: Job): Unit = {
    job._id.foreach { id =>
      eventHandlers.remove(id)
      jobs.find(_._2._id.contains(id)).foreach(t => jobs.remove(t._1))
    }
  }

  def watch(file: String, stats: Stats): FileWatch = {
    watching.getOrElseUpdate(file, new FileWatch(stats))
  }

}

/**
  * Job File Tracking Companion
  * @author lawrence.daniels@gmail.com
  */
object JobFileTracking {

  @ScalaJSDefined
  class FileWatch(var stats: Stats) extends js.Object {
    def elapsedTime: Double = js.Date.now() - stats.mtime.getTime()
  }

}
