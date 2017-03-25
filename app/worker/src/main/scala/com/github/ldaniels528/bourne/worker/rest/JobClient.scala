package com.github.ldaniels528.bourne.worker
package rest

import com.github.ldaniels528.bourne.models.JobStates.JobState
import com.github.ldaniels528.bourne.models.StatisticsLike
import com.github.ldaniels528.bourne.rest.Job
import io.scalajs.nodejs.os.OS
import io.scalajs.npm.request.RequestOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * Job REST Client
  * @author lawrence.daniels@gmail.com
  */
class JobClient(endpoint: String) extends AbstractRestClient(endpoint) {
  private val jobs = js.Dictionary[Job]()
  private val hostName = OS.hostname()

  def changeState(jobId: String, state: JobState)(implicit ec: ExecutionContext): Future[Job] = {
    patch[Job](s"job/$jobId/state/$state")
  }

  def createJob(job: Job)(implicit ec: ExecutionContext): Future[Job] = {
    post[Job](new RequestOptions(uri = getUrl("jobs"), json = job))
  }

  def isTracked(file: String): Boolean = jobs.contains(file)

  def getJobs(implicit ec: ExecutionContext): Future[js.Array[Job]] = {
    get[js.Array[Job]]("jobs")
  }

  def getNextJob()(implicit ec: ExecutionContext): Future[Option[Job]] = {
    patch[js.Array[Job]](s"jobs/checkout/$hostName") map (_.headOption)
  }

  def updateJob(job: Job)(implicit ec: ExecutionContext): Future[Job] = {
    post[Job](new RequestOptions(uri = getUrl(s"job/${job._id.orNull}"), json = job))
  }

  def updateStatistics(jobId: String, statistics: StatisticsLike)(implicit ec: ExecutionContext): Future[Job] = {
    patch[Job](new RequestOptions(uri = getUrl(s"job/$jobId/statistics"), json = statistics))
  }

  def trackJob(file: String, job: Job): Unit = jobs(file) = job

  def untrackJob(job: Job): Unit = {
    jobs.find(_._2._id == job._id).foreach(t => jobs.remove(t._1))
  }

}
