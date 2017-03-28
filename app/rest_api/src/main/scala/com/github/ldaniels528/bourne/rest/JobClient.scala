package com.github.ldaniels528.bourne.rest

import com.github.ldaniels528.bourne.models.JobStates.JobState
import com.github.ldaniels528.bourne.models.{StatisticsLike, StatusMessage}
import io.scalajs.npm.request.RequestOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * Job REST Client
  * @author lawrence.daniels@gmail.com
  */
class JobClient(endpoint: String) extends AbstractRestClient(endpoint) {

  def changeState(jobId: String, state: JobState, message: String = null)(implicit ec: ExecutionContext): Future[Job] = {
    patch[Job](new RequestOptions(uri = getUrl(s"job/$jobId/state/$state"), json = new StatusMessage(message)))
  }

  def createJob(job: Job)(implicit ec: ExecutionContext): Future[Job] = {
    post[Job](new RequestOptions(uri = getUrl("jobs"), json = job))
  }

  def getJobByID(id: String)(implicit ec: ExecutionContext): Future[Option[Job]] = {
    get[js.Array[Job]](s"job/$id").map(_.headOption)
  }

  def getJobs(implicit ec: ExecutionContext): Future[js.Array[Job]] = {
    get[js.Array[Job]]("jobs")
  }

  def getNextJob(host: String)(implicit ec: ExecutionContext): Future[Option[Job]] = {
    patch[js.Array[Job]](s"jobs/checkout/$host") map (_.headOption)
  }

  def updateJob(job: Job)(implicit ec: ExecutionContext): Future[Job] = {
    post[Job](new RequestOptions(uri = getUrl(s"job/${job._id.orNull}"), json = job))
  }

  def updateStatistics(jobId: String, statistics: StatisticsLike)(implicit ec: ExecutionContext): Future[Job] = {
    patch[Job](new RequestOptions(uri = getUrl(s"job/$jobId/statistics"), json = statistics))
  }

}
