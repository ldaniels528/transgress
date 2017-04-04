package com.github.ldaniels528.transgress.worker

import com.github.ldaniels528.transgress.rest.Job
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.Future
import scala.language.postfixOps
import scala.scalajs.js

/**
  * Job File Tracking
  * @author lawrence.daniels@gmail.com
  */
trait JobFileTracking {
  private val jobs = js.Dictionary[Job]()
  private val eventHandlers = js.Dictionary[JobControlSupport]()

  def isTracked(file: String): Boolean = jobs.contains(file)

  def lookupJob(file: String): Option[Job] = jobs.get(file)

  def registerJobController(job: Job, handler: JobControlSupport): JobControlSupport = {
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
  }

  def untrackJob(job: Job): Unit = {
    job._id.foreach { id =>
      eventHandlers.remove(id)
      jobs.find(_._2._id.contains(id)).foreach(t => jobs.remove(t._1))
    }
  }

}
