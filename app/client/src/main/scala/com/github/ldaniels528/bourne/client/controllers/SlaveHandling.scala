package com.github.ldaniels528.bourne.client.controllers

import com.github.ldaniels528.bourne.RemoteEvent.SLAVE_UPDATE
import com.github.ldaniels528.bourne.client.models.Slave
import com.github.ldaniels528.bourne.client.services.SlaveService
import io.scalajs.dom.Event
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Scope}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Slave Handling
  * @author lawrence.daniels@gmail.com
  */
trait SlaveHandling {
  self: Controller =>

  def $scope: Scope with SlaveHandlingScope

  def slaveService: SlaveService

  def toaster: Toaster

  /////////////////////////////////////////////////////////
  //    Initialization
  /////////////////////////////////////////////////////////

  $scope.slaves = js.Array()

  /////////////////////////////////////////////////////////
  //    Public Methods
  /////////////////////////////////////////////////////////

  $scope.refreshSlaves = () => {
    slaveService.getSlaves().toFuture onComplete {
      case Success(response) =>
        $scope.$apply(() => $scope.slaves = response.data)
      case Failure(e) =>
        toaster.error("Error loading slave")
        console.error(s"Error loading slave: ${e.displayMessage}")
    }
  }

  $scope.updateSlave = (aSlaves: js.UndefOr[js.Array[Slave]], aSlave: js.UndefOr[Slave]) => {
    for {
      slaves <- aSlaves
      slave <- aSlave
    } {
      slaves.indexWhere(_._id == slave._id) match {
        case -1 => slaves.push(slave)
        case index =>
          val theSlave = slaves(index)
          theSlave.concurrency = slave.concurrency
          theSlave.maxConcurrency = slave.maxConcurrency
          theSlave.lastUpdated = slave.lastUpdated
      }
    }
  }

  /**
    * Computes the slave's current utilization
    */
  $scope.utilization = (aSlave: js.UndefOr[Slave]) => {
    for {
      slave <- aSlave
      jobs <- slave.concurrency
      maxConcurrency <- slave.maxConcurrency if maxConcurrency > 0
    } yield 100.0 * (jobs / maxConcurrency.toDouble)
  }

  /////////////////////////////////////////////////////////
  //    Event Listeners
  /////////////////////////////////////////////////////////

  $scope.$on(SLAVE_UPDATE, (_: Event, slave: Slave) => {
    $scope.$apply(() => $scope.updateSlave($scope.slaves, slave))
  })

}

/**
  * Slave Handling Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait SlaveHandlingScope extends js.Any {
  self: Scope =>

  // variables
  var slaves: js.Array[Slave] = js.native

  // functions
  var refreshSlaves: js.Function0[Unit] = js.native
  var updateSlave: js.Function2[js.UndefOr[js.Array[Slave]], js.UndefOr[Slave], Unit] = js.native
  var utilization: js.Function1[js.UndefOr[Slave], js.UndefOr[Double]] = js.native

}