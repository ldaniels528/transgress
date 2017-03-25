package com.github.ldaniels528.bourne.models

import scala.scalajs.js

/**
  * Represents an Operation-like model
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait OperationLike extends js.Object {

  def execute(): js.Any = js.native

}
