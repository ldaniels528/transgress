package com.github.ldaniels528.bourne.client
package models

import com.github.ldaniels528.bourne.models._

import scala.scalajs.js

/**
  * Represents a workflow model
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait Workflow extends WorkflowLike with Expandable {
  var _id: js.UndefOr[String] = js.native
  var name: js.UndefOr[String] = js.native
  var input: js.UndefOr[SourceLike] = js.native
  var outputs: js.UndefOr[js.Array[SourceLike]] = js.native
  var events: js.UndefOr[js.Dictionary[OperationLike]] = js.native
  var variables: js.UndefOr[js.Array[VariableLike]] = js.native

}
