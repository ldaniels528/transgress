package com.github.ldaniels528.bourne.client.controllers

import com.github.ldaniels528.bourne.RemoteEvent.JOB_UPDATE
import com.github.ldaniels528.bourne.client.models.Job
import com.github.ldaniels528.bourne.client.services.JobService
import com.github.ldaniels528.bourne.models.JobStates
import com.github.ldaniels528.bourne.models.JobStates._
import io.scalajs.dom.Event
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Scope}
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * Job Handling
  * @author lawrence.daniels@gmail.com
  */
trait JobHandling {
  self: Controller =>

  def jobService: JobService

  def $scope: Scope with JobHandlingScope

  def toaster: Toaster

  /////////////////////////////////////////////////////////
  //    Initialization
  /////////////////////////////////////////////////////////

  $scope.jobs = js.Array()

  /////////////////////////////////////////////////////////
  //    Public Methods
  /////////////////////////////////////////////////////////

  /**
    * Returns only the currently running jobs
    */
  $scope.getRunningJobs = () => $scope.jobs.filterNot(_.state.contains(JobStates.SUCCESS))

  /**
    * Returns a colored bulb based on the status of the given job
    */
  $scope.getJobStatusBulb = (aJob: js.UndefOr[Job]) => aJob flatMap { job =>
    job.state flatMap {
      case NEW => "images/statuses/offlight.png"
      case CLAIMED => "images/statuses/bluelight.png"
      case PAUSED => "images/statuses/yellowlight.gif"
      case QUEUED => "images/statuses/bluelight.png"
      case RUNNING => "images/statuses/loading16.gif"
      case STOPPED => "images/statuses/redlight.png"
      case SUCCESS => "images/statuses/greenlight.png"
      case _ => js.undefined
    }
  }

  /**
    * Returns a class representing the status of the given job
    */
  $scope.getJobStatusClass = (aJob: js.UndefOr[Job]) => aJob flatMap { job =>
    job.state flatMap {
      case NEW => "status_new"
      case CLAIMED => "status_claimed"
      case PAUSED => "status_paused"
      case QUEUED => "status_queued"
      case RUNNING => "status_running"
      case STOPPED => "status_stopped"
      case SUCCESS => "status_success"
      case _ => js.undefined
    }
  }

  $scope.isPausable = (aJob: js.UndefOr[Job]) => aJob.exists { job =>
    job.state.contains(JobStates.RUNNING) && !isJobOperation
  }

  $scope.isResumable = (aJob: js.UndefOr[Job]) => aJob.exists { job =>
    (job.state.contains(JobStates.PAUSED) || job.state.contains(JobStates.STOPPED)) && !isJobOperation
  }

  $scope.isRunning = (aJob: js.UndefOr[Job]) => aJob exists (_.state.contains(RUNNING))

  $scope.isStoppable = (aJob: js.UndefOr[Job]) => aJob.exists { job =>
    (job.state.contains(JobStates.PAUSED) || job.state.contains(JobStates.RUNNING)) && !isJobOperation
  }

  /**
    * Selects a job
    */
  $scope.selectJob = (aJob: js.UndefOr[Job]) => {
    $scope.selectedJob = aJob.flat
  }

  $scope.refreshJobs = (aJobStates: js.UndefOr[js.Array[JobState]]) => {
    val jobStates = aJobStates getOrElse JobStates.values.toJSArray
    jobService.getJobs(jobStates: _*).toFuture onComplete {
      case Success(response) =>
        $scope.$apply(() => {
          response.data.foreach(job => $scope.updateJob($scope.jobs, job))
        })
      case Failure(e) =>
        console.error(e.displayMessage)
    }
  }

  $scope.updateJob = (aJobs: js.UndefOr[js.Array[Job]], aJob: js.UndefOr[Job]) => {
    for {
      jobs <- aJobs
      job <- aJob
    } {
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

  private def isJobOperation = $scope.pausing.isTrue || $scope.resuming.isTrue || $scope.stopping.isTrue

  /////////////////////////////////////////////////////////
  //    Event Listeners
  /////////////////////////////////////////////////////////

  $scope.$on(JOB_UPDATE, (_: Event, job: Job) => {
    $scope.$apply(() => $scope.updateJob($scope.jobs, job))
  })

}

/**
  * Job Handling Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait JobHandlingScope extends js.Any {
  self: Scope =>

  // variables
  var jobs: js.Array[Job] = js.native
  var pausing: js.UndefOr[Boolean] = js.native
  var resuming: js.UndefOr[Boolean] = js.native
  var stopping: js.UndefOr[Boolean] = js.native
  var selectedJob: js.UndefOr[Job] = js.native

  // functions
  var getRunningJobs: js.Function0[js.Array[Job]] = js.native
  var getJobStatusBulb: js.Function1[js.UndefOr[Job], js.UndefOr[String]] = js.native
  var getJobStatusClass: js.Function1[js.UndefOr[Job], js.UndefOr[String]] = js.native
  var isPausable: js.Function1[js.UndefOr[Job], Boolean] = js.native
  var isResumable: js.Function1[js.UndefOr[Job], Boolean] = js.native
  var isRunning: js.Function1[js.UndefOr[Job], Boolean] = js.native
  var isStoppable: js.Function1[js.UndefOr[Job], Boolean] = js.native
  var refreshJobs: js.Function1[js.UndefOr[js.Array[JobState]], Unit] = js.native
  var selectJob: js.Function1[js.UndefOr[Job], Unit] = js.native
  var updateJob: js.Function2[js.UndefOr[js.Array[Job]], js.UndefOr[Job], Unit] = js.native

}