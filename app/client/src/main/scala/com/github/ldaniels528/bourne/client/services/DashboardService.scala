package com.github.ldaniels528.bourne.client
package services

import com.github.ldaniels528.bourne.client.models.Job
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}

import scala.scalajs.js

/**
  * Dashboard Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
case class DashboardService($http: Http) extends Service {

  def getJobs: js.Promise[HttpResponse[js.Array[Job]]] = $http.get("/api/jobs")

}