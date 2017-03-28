package com.github.ldaniels528.bourne.client
package controllers

import com.github.ldaniels528.bourne.client.models.Job
import com.github.ldaniels528.bourne.client.services.JobService
import com.github.ldaniels528.bourne.models.JobStates
import io.scalajs.dom.html.browser._
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Interval, Scope, injected}
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Dashboard Controller
  * @author lawrence.daniels@gmail.com
  */
case class DashboardController($scope: DashboardScope, $interval: Interval, toaster: Toaster,
                               @injected("JobService") jobService: JobService)
  extends Controller with JobHandling {

  /////////////////////////////////////////////////////////
  //    Public Methods
  /////////////////////////////////////////////////////////

  /**
    * Initializes the controller
    */
  $scope.init = () => {
    console.info(s"Initializing ${getClass.getSimpleName}...")
  }

  $scope.isPausable = (aJob: js.UndefOr[Job]) => aJob.exists { job =>
    job.state.contains(JobStates.RUNNING) && !isJobOperation
  }

  $scope.isResumable = (aJob: js.UndefOr[Job]) => aJob.exists { job =>
    (job.state.contains(JobStates.PAUSED) || job.state.contains(JobStates.STOPPED)) && !isJobOperation
  }

  $scope.isStoppable = (aJob: js.UndefOr[Job]) => aJob.exists { job =>
    (job.state.contains(JobStates.PAUSED) || job.state.contains(JobStates.RUNNING)) && !isJobOperation
  }

  private def isJobOperation = $scope.pausing.isTrue || $scope.resuming.isTrue || $scope.stopping.isTrue

  /**
    * Pauses the given job
    */
  $scope.pauseJob = (aJob: js.UndefOr[Job]) => {
    for {
      job <- aJob
      id <- job._id
    } {
      $scope.pausing = true
      jobService.pauseJob(id).toFuture onComplete {
        case Success(response) =>
          $scope.$apply { () =>
            $scope.pausing = false
            updateJob($scope.jobs, response.data)
          }
        case Failure(e) =>
          $scope.pausing = false
          toaster.error("Failed to pause job")
          console.error(s"Failed to pause job: ${e.displayMessage}")
      }
    }
  }

  /**
    * Resumes the given job
    */
  $scope.resumeJob = (aJob: js.UndefOr[Job]) => {
    for {
      job <- aJob
      id <- job._id
    } {
      $scope.resuming = true
      jobService.resumeJob(id).toFuture onComplete {
        case Success(response) =>
          $scope.$apply { () =>
            $scope.resuming = false
            updateJob($scope.jobs, response.data)
          }
        case Failure(e) =>
          $scope.resuming = false
          toaster.error("Failed to resume job")
          console.error(s"Failed to resume job: ${e.displayMessage}")
      }
    }
  }

  /**
    * Stops the given job
    */
  $scope.stopJob = (aJob: js.UndefOr[Job]) => {
    for {
      job <- aJob
      id <- job._id
    } {
      $scope.stopping = true
      jobService.stopJob(id).toFuture onComplete {
        case Success(response) =>
          $scope.$apply { () =>
            $scope.stopping = false
            updateJob($scope.jobs, response.data)
          }
        case Failure(e) =>
          $scope.stopping = false
          toaster.error("Failed to stop job")
          console.error(s"Failed to stop job: ${e.displayMessage}")
      }
    }
  }

}

/**
  * Dashboard Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait DashboardScope extends Scope with JobHandlingScope {
  // variables
  var pausing: js.UndefOr[Boolean] = js.native
  var resuming: js.UndefOr[Boolean] = js.native
  var stopping: js.UndefOr[Boolean] = js.native

  // functions
  var init: js.Function0[Unit] = js.native
  var isPausable: js.Function1[js.UndefOr[Job], Boolean] = js.native
  var isResumable: js.Function1[js.UndefOr[Job], Boolean] = js.native
  var isStoppable: js.Function1[js.UndefOr[Job], Boolean] = js.native
  var pauseJob: js.Function1[js.UndefOr[Job], Unit] = js.native
  var resumeJob: js.Function1[js.UndefOr[Job], Unit] = js.native
  var stopJob: js.Function1[js.UndefOr[Job], Unit] = js.native

}
