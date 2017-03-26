package com.github.ldaniels528.bourne.client
package controllers

import com.github.ldaniels528.bourne.client.services.JobService
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Interval, Scope, injected}

import scala.scalajs.js

/**
  * Activity Controller
  * @author lawrence.daniels@gmail.com
  */
class ActivityController($scope: ActivityScope, $interval: Interval, toaster: Toaster,
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

}

/**
  * Activity Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait ActivityScope extends Scope {

  // functions
  var init: js.Function0[Unit] = js.native

}
