package com.github.ldaniels528.bourne.client
package controllers

import com.github.ldaniels528.bourne.AppConstants._
import com.github.ldaniels528.bourne.client.models.{Job, Slave}
import com.github.ldaniels528.bourne.client.services.{JobService, SlaveService}
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs._
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.util.DurationHelper._

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * Main Controller
  * @author lawrence.daniels@gmail.com
  */
case class MainController($scope: MainScope, $interval: Interval, $location: Location, toaster: Toaster,
                          @injected("JobService") jobService: JobService,
                          @injected("SlaveService") slaveService: SlaveService)
  extends Controller with CollapseExpandHandling with JobHandling with SlaveHandling with TabHandling {

  /////////////////////////////////////////////////////////
  //    Initialization
  /////////////////////////////////////////////////////////

  $scope.version = String.valueOf(Version)

  /////////////////////////////////////////////////////////
  //    Public Methods
  /////////////////////////////////////////////////////////

  /**
    * Initializes the controller
    */
  $scope.init = () => {
    console.info(s"Initializing ${getClass.getSimpleName}...")

    // get the list of jobs now
    $scope.refreshJobs(js.undefined)
    $scope.refreshSlaves()

    // and update them every 3 minutes
    $interval(() => $scope.refreshJobs(js.undefined), 3.minute)
  }

  $scope.getSlaveJobs = (aSlave: js.UndefOr[Slave]) => {
    for {
      slave <- aSlave
      slaveID <- slave._id
    } yield $scope.jobs.filter(_.slaveID == slaveID)
  }

  private def getSlaveForJob(job: Job) = {
    $scope.slaves.find(_._id == job.slaveID)
  }

  /**
    * Pauses the given job
    */
  $scope.pauseJob = (aJob: js.UndefOr[Job]) => {
    for {
      job <- aJob
      jobId <- job._id
      slave <- getSlaveForJob(job).orUndefined
      slaveId <- slave._id
    } {
      $scope.pausing = true
      jobService.pauseJob(jobId, slaveId).toFuture onComplete {
        case Success(response) =>
          $scope.$apply { () =>
            $scope.pausing = false
            $scope.updateJob($scope.jobs, response.data)
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
      jobId <- job._id
      slave <- getSlaveForJob(job).orUndefined
      slaveId <- slave._id
    } {
      $scope.resuming = true
      jobService.resumeJob(jobId, slaveId).toFuture onComplete {
        case Success(response) =>
          $scope.$apply { () =>
            $scope.resuming = false
            $scope.updateJob($scope.jobs, response.data)
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
      jobId <- job._id
      slave <- getSlaveForJob(job).orUndefined
      slaveId <- slave._id
    } {
      $scope.stopping = true
      jobService.stopJob(jobId, slaveId).toFuture onComplete {
        case Success(response) =>
          $scope.$apply { () =>
            $scope.stopping = false
            $scope.updateJob($scope.jobs, response.data)
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
  * Main Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait MainScope extends Scope
  with CollapseExpandHandlingScope with JobHandlingScope with SlaveHandlingScope with TabHandlingScope {

  // variables
  var version: String = js.native

  // functions
  var init: js.Function0[Unit] = js.native
  var getSlaveJobs: js.Function1[js.UndefOr[Slave], js.UndefOr[js.Array[Job]]] = js.native
  var pauseJob: js.Function1[js.UndefOr[Job], Unit] = js.native
  var resumeJob: js.Function1[js.UndefOr[Job], Unit] = js.native
  var stopJob: js.Function1[js.UndefOr[Job], Unit] = js.native

}

