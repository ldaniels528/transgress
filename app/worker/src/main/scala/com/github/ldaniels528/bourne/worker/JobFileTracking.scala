package com.github.ldaniels528.bourne.worker

import com.github.ldaniels528.bourne.rest.Job
import com.github.ldaniels528.bourne.worker.JobFileTracking.FileWatch
import io.scalajs.nodejs.fs.Stats

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Job File Tracking
  * @author lawrence.daniels@gmail.com
  */
trait JobFileTracking {
  private val jobs = js.Dictionary[Job]()
  private val watching = js.Dictionary[FileWatch]()

  def isTracked(file: String): Boolean = jobs.contains(file)

  def isWatched(file: String): Boolean = watching.contains(file)

  def isWatchedOrTracked(file: String): Boolean = isTracked(file) || isWatched(file)

  def trackJob(file: String, job: Job): Unit = {
    jobs(file) = job
    watching.remove(file)
  }

  def untrackJob(job: Job): Unit = {
    jobs.find(_._2._id == job._id).foreach(t => jobs.remove(t._1))
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
