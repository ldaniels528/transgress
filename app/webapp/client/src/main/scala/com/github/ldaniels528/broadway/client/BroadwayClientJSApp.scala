package com.github.ldaniels528.broadway
package client

import com.github.ldaniels528.broadway.client.controllers.{DashboardController, MainController}
import com.github.ldaniels528.broadway.client.services.{DashboardService, WebSocketService}
import io.scalajs.npm.angularjs.uirouter.{RouteProvider, RouteTo}
import io.scalajs.npm.angularjs.{Module, Scope, Timeout, angular}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

/**
  * Broadway Client JavaScript Application
  * @author lawrence.daniels@gmail.com
  */
object BroadwayClientJSApp extends js.JSApp {

  @JSExport
  override def main(): Unit = {
    // create the application
    val module = angular.createModule("broadway",
      js.Array("ngAnimate", "ngCookies", "ngRoute", "ngSanitize", "nvd3", "angularFileUpload", "toaster", "ui.bootstrap"))

    // add the controllers and services
    configureServices(module)
    configureFactories(module)
    configureControllers(module)

    // define the routes
    module.config({ ($routeProvider: RouteProvider) =>
      // configure the routes
      $routeProvider
        .when("/dashboard", new RouteTo(templateUrl = "/views/dashboard/index.html", controller = classOf[DashboardController].getSimpleName))
        .when("/dashboard/:id", new RouteTo(templateUrl = "/views/dashboard/index.html", controller = classOf[DashboardController].getSimpleName))
        .otherwise(new RouteTo(redirectTo = "/dashboard"))
      ()
    })

    // initialize the application
    module.run({ ($rootScope: Scope, $timeout: Timeout, WebSocketService: WebSocketService) =>
      // initialize the web socket service
      WebSocketService.init()
    })
  }

  private def configureControllers(module: Module) {
    module.controllerOf[DashboardController]("DashboardController")
    module.controllerOf[MainController]("MainController")
  }

  private def configureFactories(module: Module): Unit = {
    //module.factoryOf[UserFactory]("UserFactory")
  }

  private def configureServices(module: Module) {
    module.serviceOf[DashboardService]("DashboardService")
    module.serviceOf[WebSocketService]("WebSocketService")
  }

}
