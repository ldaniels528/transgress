package com.github.ldaniels528.bourne.client.controllers

import com.github.ldaniels528.bourne.client.models.Job
import com.github.ldaniels528.bourne.client.services.JobService
import com.github.ldaniels528.bourne.models.JobStates
import com.github.ldaniels528.bourne.models.JobStates.JobState
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Scope}

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Job Handling
  * @author lawrence.daniels@gmail.com
  */
trait JobHandling {
  self: Controller =>

  def jobService: JobService

  def $scope: JobHandlingScope

  def toaster: Toaster

  def refreshJobs(jobStates: Seq[JobState] = JobStates.values.toSeq)(implicit ec: ExecutionContext): Unit = {
    jobService.getJobs(jobStates: _*).toFuture onComplete {
      case Success(response) =>
        $scope.$apply(() => {
          response.data.foreach(job => updateJob($scope.jobs, job))
        })
      case Failure(e) =>
        console.error(e.displayMessage)
    }
  }

  def updateJob(jobs: js.Array[Job], job: Job): Unit = {
    jobs.indexWhere(_._id == job._id) match {
      case -1 => jobs.push(job)
      case index =>
        val theJob = jobs(index)
        theJob.state = job.state
        theJob.processingHost = job.processingHost
        theJob.statistics = job.statistics
    }
  }

}

@js.native
trait JobHandlingScope extends Scope {
  // variables
  var jobs: js.Array[Job] = js.native

}