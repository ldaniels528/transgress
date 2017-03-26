package com.github.ldaniels528.bourne.client
package controllers

import com.github.ldaniels528.bourne.AppConstants._
import com.github.ldaniels528.bourne.RemoteEvent._
import com.github.ldaniels528.bourne.client.models.{Expandable, Job}
import com.github.ldaniels528.bourne.client.services.JobService
import com.github.ldaniels528.bourne.models.JobStates._
import io.scalajs.dom.Event
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs._
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.util.DurationHelper._
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Main Controller
  * @author lawrence.daniels@gmail.com
  */
case class MainController($scope: MainScope, $interval: Interval, $location: Location, toaster: Toaster,
                          @injected("JobService") jobService: JobService)
  extends Controller with JobHandling {

  /////////////////////////////////////////////////////////
  //    Variables
  /////////////////////////////////////////////////////////

  $scope.version = String.valueOf(Version)

  $scope.jobs = js.Array()

  $scope.tabs = js.Array(
    new AppTab(name = "Activity", uri = "/activity", icon = "fa-tasks"),
    new AppTab(name = "Dashboard", uri = "/dashboard", icon = "fa-stack-overflow"),
    new AppTab(name = "Slaves", uri = "/slaves", icon = "fa-android"),
    new AppTab(name = "Triggers", uri = "/triggers", icon = "fa-cogs"),
    new AppTab(name = "Workflows", uri = "/workflows", icon = "fa-th-large")
  )

  // the first tab is active
  $scope.selectedTab = $scope.tabs(0)

  /////////////////////////////////////////////////////////
  //    Public Methods
  /////////////////////////////////////////////////////////

  /**
    * Initializes the controller
    */
  $scope.init = () => {
    console.info(s"Initializing ${getClass.getSimpleName}...")

    // get the list of jobs now
    refreshJobs()

    // and update them every 5 minutes
    $interval(() => refreshJobs(), 5.minute)
  }

  /////////////////////////////////////////////////////////
  //    Public Methods
  /////////////////////////////////////////////////////////

  $scope.changeTab = (aTab: js.UndefOr[AppTab]) => aTab foreach { tab =>
    $scope.selectedTab = tab
    $location.url(tab.uri)
  }

  $scope.collapseExpand = (anExpandable: js.UndefOr[Expandable]) => anExpandable foreach { expandable =>
    expandable.expanded = !expandable.expanded.isTrue
  }

  /**
    * Returns a colored bulb based on the status of the given job
    */
  $scope.getStatusBulb = (aJob: js.UndefOr[Job]) => aJob flatMap { job =>
    job.state flatMap {
      case NEW => "images/statuses/offlight.png"
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
  $scope.getStatusClass = (aJob: js.UndefOr[Job]) => aJob flatMap { job =>
    job.state flatMap {
      case NEW => "status_new"
      case PAUSED => "status_paused"
      case QUEUED => "status_queued"
      case RUNNING => "status_running"
      case STOPPED => "status_stopped"
      case SUCCESS => "status_success"
      case _ => js.undefined
    }
  }

  $scope.isRunning = (aJob: js.UndefOr[Job]) => aJob exists (_.state.contains(RUNNING))

  /**
    * Selects a job
    */
  $scope.selectJob = (aJob: js.UndefOr[Job]) => {
    $scope.selectedJob = aJob.flat
  }

  /////////////////////////////////////////////////////////
  //    Event Handlers
  /////////////////////////////////////////////////////////

  $scope.$on(JOB_UPDATE, (_: Event, job: Job) => $scope.$apply(() => updateJob($scope.jobs, job)))

}

/**
  * Main Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait MainScope extends JobHandlingScope {
  // variables
  var tabs: js.Array[AppTab] = js.native
  var selectedJob: js.UndefOr[Job] = js.native
  var selectedTab: AppTab = js.native
  var version: String = js.native

  // functions
  var init: js.Function0[Unit] = js.native
  var changeTab: js.Function1[js.UndefOr[AppTab], Unit] = js.native
  var collapseExpand: js.Function1[js.UndefOr[Expandable], Unit] = js.native
  var getStatusBulb: js.Function1[js.UndefOr[Job], js.UndefOr[String]] = js.native
  var getStatusClass: js.Function1[js.UndefOr[Job], js.UndefOr[String]] = js.native
  var isRunning: js.Function1[js.UndefOr[Job], Boolean] = js.native
  var selectJob: js.Function1[js.UndefOr[Job], Unit] = js.native

}

/**
  * Represents a tab
  * @param name the name of the tab
  * @param icon the icon of the tab
  */
@ScalaJSDefined
class AppTab(val name: String, val uri: String, val icon: String) extends js.Object
