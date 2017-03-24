package com.github.ldaniels528.bourne.client.controllers

import io.scalajs.npm.angularjs.{Controller, Scope}

import scala.scalajs.js

/**
  * Main Controller
  * @author lawrence.daniels@gmail.com
  */
class MainController($scope: MainScope) extends Controller {
  $scope.version = "0.0.1"

}

/**
  * Main Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait MainScope extends Scope {
  var version: String = js.native

}