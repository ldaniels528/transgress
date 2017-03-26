package com.github.ldaniels528.bourne.rest

import com.github.ldaniels528.bourne.models.JobStates.JobState
import com.github.ldaniels528.bourne.models.StatisticsLike
import io.scalajs.nodejs.os.OS
import io.scalajs.npm.request.RequestOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * Job REST Client
  * @author lawrence.daniels@gmail.com
  */
class JobClient(endpoint: String) extends AbstractRestClient(endpoint) {

  def changeState(jobId: String, state: JobState)(implicit ec: ExecutionContext): Future[Job] = {
    patch[Job](s"job/$jobId/state/$state")
  }

  def createJob(job: Job)(implicit ec: ExecutionContext): Future[Job] = {
    post[Job](new RequestOptions(uri = getUrl("jobs"), json = job))
  }

  def getJobs(implicit ec: ExecutionContext): Future[js.Array[Job]] = {
    get[js.Array[Job]]("jobs")
  }

  def getNextJob()(implicit ec: ExecutionContext): Future[Option[Job]] = {
    patch[js.Array[Job]](s"jobs/checkout/${OS.hostname()}") map (_.headOption)
  }

  def updateJob(job: Job)(implicit ec: ExecutionContext): Future[Job] = {
    post[Job](new RequestOptions(uri = getUrl(s"job/${job._id.orNull}"), json = job))
  }

  def updateStatistics(jobId: String, statistics: StatisticsLike)(implicit ec: ExecutionContext): Future[Job] = {
    patch[Job](new RequestOptions(uri = getUrl(s"job/$jobId/statistics"), json = statistics))
  }

}
