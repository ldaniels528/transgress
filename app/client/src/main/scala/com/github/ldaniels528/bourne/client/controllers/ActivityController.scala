package com.github.ldaniels528.bourne.client
package controllers

import com.github.ldaniels528.bourne.client.models.Job
import com.github.ldaniels528.bourne.models.JobStates
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Interval, Scope}

import scala.scalajs.js

/**
  * Activity Controller
  * @author lawrence.daniels@gmail.com
  */
class ActivityController($scope: ActivityScope, $interval: Interval, toaster: Toaster)
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
trait ActivityScope extends Scope with JobHandlingScope {

  // functions
  var init: js.Function0[Unit] = js.native

}
