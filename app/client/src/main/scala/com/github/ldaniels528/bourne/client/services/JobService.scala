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
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
case class JobService($http: Http) extends Service {

  def getJobs(states: JobState*)(implicit ec: ExecutionContext): js.Promise[HttpResponse[js.Array[Job]]] = {
    $http.get(if(states.nonEmpty) s"/api/jobs?${states.map(s => s"states=$s").mkString("&")}" else "/api/jobs")
  }

}
