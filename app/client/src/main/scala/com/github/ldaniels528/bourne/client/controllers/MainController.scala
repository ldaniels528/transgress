package com.github.ldaniels528.bourne.client
package controllers

import com.github.ldaniels528.bourne.client.models.Expandable
import com.github.ldaniels528.bourne.client.services.JobService
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

  $scope.version = "0.0.1"

  $scope.jobs = js.Array()

  $scope.tabs = js.Array(
    new AppTab(name = "Dashboard", uri = "/dashboard", icon = "fa-stack-overflow"),
    new AppTab(name = "Activity", uri = "/activity", icon = "fa-tasks"),
    new AppTab(name = "Triggers", uri = "/triggers", icon = "fa-cogs"),
    new AppTab(name = "Workflows", uri = "/workflows", icon = "fa-th-large")
  )

  $scope.tabs(0).active = true

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

    // and update them every 5 seconds
    $interval(() => refreshJobs(), 5.seconds)
  }

  /////////////////////////////////////////////////////////
  //    Public Methods
  /////////////////////////////////////////////////////////

  $scope.changeTab = (aTab: js.UndefOr[AppTab]) => aTab foreach { tab =>
    $scope.tabs.foreach(_.active = false)
    tab.active = true
    $location.url(tab.uri)
  }

  $scope.collapseExpand = (anExpandable: js.UndefOr[Expandable]) => anExpandable foreach { expandable =>
    expandable.expanded = !expandable.expanded.isTrue
  }

}

/**
  * Main Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait MainScope extends JobHandlingScope {
  // variables
  var version: String = js.native
  var tabs: js.Array[AppTab] = js.native

  // functions
  var changeTab: js.Function1[js.UndefOr[AppTab], Unit] = js.native
  var collapseExpand: js.Function1[js.UndefOr[Expandable], Unit] = js.native
  var init: js.Function0[Unit] = js.native

}

/**
  * Represents a tab
  * @param name   the name of the tab
  * @param icon   the icon of the tab
  * @param active indicates whether the tab is active
  */
@ScalaJSDefined
class AppTab(val name: String,
             val uri: String,
             val icon: String,
             var active: Boolean = false) extends js.Object
