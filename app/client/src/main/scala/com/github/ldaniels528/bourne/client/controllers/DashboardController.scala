package com.github.ldaniels528.bourne.client
package controllers

import com.github.ldaniels528.bourne.client.models.Job
import com.github.ldaniels528.bourne.client.services.JobService
import com.github.ldaniels528.bourne.models.JobStates._
import io.scalajs.dom.html.browser._
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Interval, Scope, injected}
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
  * Dashboard Controller
  * @author lawrence.daniels@gmail.com
  */
class DashboardController($scope: DashboardScope, $interval: Interval, toaster: Toaster,
                          @injected("JobService") jobService: JobService)
  extends Controller {

  /////////////////////////////////////////////////////////
  //    Public Methods
  /////////////////////////////////////////////////////////

  /**
    * Initializes the controller
    */
  $scope.init = () => {
    console.info(s"Initializing ${getClass.getSimpleName}...")
  }

  /**
    * Returns a colored bulb based on the status of the given job
    */
  $scope.getStatusBulb = (aJob: js.UndefOr[Job]) => aJob flatMap { job =>
    job.state flatMap {
      case NEW => "images/statuses/offlight.png"
      case QUEUED => "images/statuses/yellowlight.gif"
      case RUNNING => "images/statuses/loading16.gif"
      case STOPPED => "images/statuses/redlight.png"
      case SUCCESS => "images/statuses/greenlight.png"
      case _ => js.undefined
    }
  }

  /**
    * Returns a class representing the status of the given job
    */
  $scope.getStatusClass = (aJob: js.UndefOr[Job]) => aJob flatMap { job =>
    job.state flatMap {
      case NEW => "status_pending"
      case QUEUED => "status_queued"
      case RUNNING => "status_active"
      case STOPPED => "status_failed"
      case SUCCESS => "status_success"
      case _ => js.undefined
    }
  }

  $scope.isRunning = (aJob: js.UndefOr[Job]) => aJob exists (_.state.contains(RUNNING))

}

/**
  * Dashboard Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait DashboardScope extends Scope {

  // functions
  var init: js.Function0[Unit] = js.native
  var getStatusBulb: js.Function1[js.UndefOr[Job], js.UndefOr[String]] = js.native
  var getStatusClass: js.Function1[js.UndefOr[Job], js.UndefOr[String]] = js.native
  var isRunning: js.Function1[js.UndefOr[Job], Boolean] = js.native

}
