package com.github.ldaniels528.bourne.client
package services

import com.github.ldaniels528.bourne.client.models.Job
import com.github.ldaniels528.bourne.models.JobStates.JobState
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Job Service
  * @author lawrence.daniels@gmail.com
  */
class JobService($http: Http) extends Service {

  /**
    * Retrieves jobs by state
    * @param states the given [[JobState states]]
    * @param ec     the [[ExecutionContext]]
    * @return the resultant array of [[Job]]s
    */
  def getJobs(states: JobState*)(implicit ec: ExecutionContext): js.Promise[HttpResponse[js.Array[Job]]] = {
    $http.get(if (states.nonEmpty) s"/api/jobs?states=${states.mkString("|")}" else "/api/jobs")
  }

  /**
    * Pauses the given job
    * @param jobId the given job ID
    * @return the [[Job updated job]]
    */
  def pauseJob(jobId: String): js.Promise[HttpResponse[Job]] = $http.get[Job](s"/api/job/$jobId/pause")

  /**
    * Resumes the given job
    * @param jobId the given job ID
    * @return the [[Job updated job]]
    */
  def resumeJob(jobId: String): js.Promise[HttpResponse[Job]] = $http.get[Job](s"/api/job/$jobId/resume")

  /**
    * Stops the given job
    * @param jobId the given job ID
    * @return the [[Job updated job]]
    */
  def stopJob(jobId: String): js.Promise[HttpResponse[Job]] = $http.get[Job](s"/api/job/$jobId/stop")

}
