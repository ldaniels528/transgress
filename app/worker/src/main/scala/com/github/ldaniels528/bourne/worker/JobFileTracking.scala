package com.github.ldaniels528.bourne.worker

import com.github.ldaniels528.bourne.rest.Job
import com.github.ldaniels528.bourne.worker.JobFileTracking.FileWatch
import com.github.ldaniels528.bourne.worker.devices.InputDevice
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
  private val devices = js.Dictionary[InputDevice]()
  private val watching = js.Dictionary[FileWatch]()

  def isTracked(file: String): Boolean = jobs.contains(file)

  def isWatched(file: String): Boolean = watching.contains(file)

  def isWatchedOrTracked(file: String): Boolean = isTracked(file) || isWatched(file)

  def lookupJob(file: String): Option[Job] = jobs.get(file)

  def startJob(job: Job, inputDevice: InputDevice): Unit = {
    devices(job._id.orNull) = inputDevice
  }

  def pauseJob(job: Job): Future[Boolean] = {
    val result = for {
      _id <- job._id.toOption
      device <- devices.get(_id)
    } yield device

    result match {
      case Some(device) => device.pause()
      case None => Future.failed(js.JavaScriptException(s"No device found for job ${job._id}"))
    }
  }

  def resumeJob(job: Job): Future[Boolean] = {
    val result = for {
      _id <- job._id.toOption
      device <- devices.get(_id)
    } yield device

    result match {
      case Some(device) => device.resume()
      case None => Future.failed(js.JavaScriptException(s"No device found for job ${job._id}"))
    }
  }

  def stopJob(job: Job): Future[Boolean] = {
    val result = for {
      _id <- job._id.toOption
      device <- devices.get(_id)
    } yield device

    result match {
      case Some(device) => device.stop()
      case None => Future.failed(js.JavaScriptException(s"No device found for job ${job._id}"))
    }
  }

  def trackJob(file: String, job: Job): Unit = {
    jobs(file) = job
    watching.remove(file)
  }

  def untrackJob(job: Job): Unit = {
    job._id.foreach { id =>
      devices.remove(id)
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
