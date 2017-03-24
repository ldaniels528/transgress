package com.github.ldaniels528.bourne.client.controllers

import com.github.ldaniels528.bourne.client.models.Job
import com.github.ldaniels528.bourne.client.services.DashboardService
import io.scalajs.dom.html.browser._
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Interval, Scope, injected}
import io.scalajs.util.DurationHelper._

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Dashboard Controller
  * @author lawrence.daniels@gmail.com
  */
class DashboardController($scope: DashboardScope, $interval: Interval, toaster: Toaster,
                          @injected("DashboardService") dashboardService: DashboardService)
  extends Controller {

  /////////////////////////////////////////////////////////
  //    Variables
  /////////////////////////////////////////////////////////

  $scope.jobs = js.Array()

  /////////////////////////////////////////////////////////
  //    Public Methods
  /////////////////////////////////////////////////////////

  /**
    * Initializes the controller
    */
  $scope.init = () => {
    // get the list of jobs now
    retrieveActiveJobs()

    // and update them every 15 seconds
    $interval(() => retrieveActiveJobs(), 15.seconds)
  }

  /**
    * Returns a colored bulb based on the status of the given job
    */
  $scope.getStatusBulb = (aJob: js.UndefOr[Job]) => aJob flatMap { job =>
    job.state flatMap {
      case "NEW" => "images/statuses/offlight.png"
      case "QUEUED" => "images/statuses/yellowlight.gif"
      case "RUNNING" => "images/statuses/loading16.gif"
      case "STOPPED" => "images/statuses/redlight.png"
      case "SUCCESS" => "images/statuses/greenlight.png"
      case _ => js.undefined
    }
  }

  /**
    * Returns a class representing the status of the given job
    */
  $scope.getStatusClass = (aJob: js.UndefOr[Job]) => aJob flatMap { job =>
    job.state flatMap {
      case "NEW" => "status_pending"
      case "QUEUED" => "status_queued"
      case "RUNNING" => "status_active"
      case "STOPPED" => "status_failed"
      case "SUCCESS" => "status_success"
      case _ => js.undefined
    }
  }

  $scope.isRunning = (aJob: js.UndefOr[Job]) => aJob exists { job =>
    job.state.contains("RUNNING")
  }

  /////////////////////////////////////////////////////////
  //    Private Methods
  /////////////////////////////////////////////////////////

  private def retrieveActiveJobs() {
    dashboardService.getJobs.toFuture onComplete {
      case Success(response) =>
        console.log(s"Loaded ${response.data.length} job(s)")
        $scope.$apply(() => $scope.jobs = response.data)
      case Failure(e) =>
        toaster.error("Initialization Error", e.displayMessage)
        console.error(e.displayMessage)
    }
  }

}

/**
  * Dashboard Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait DashboardScope extends Scope {
  // variables
  var jobs: js.Array[Job] = js.native

  // functions
  var init: js.Function0[Unit] = js.native
  var getStatusBulb: js.Function1[js.UndefOr[Job], js.UndefOr[String]] = js.native
  var getStatusClass: js.Function1[js.UndefOr[Job], js.UndefOr[String]] = js.native
  var isRunning: js.Function1[js.UndefOr[Job], Boolean] = js.native

}