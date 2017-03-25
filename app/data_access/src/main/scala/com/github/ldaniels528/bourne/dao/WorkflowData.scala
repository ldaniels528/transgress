package com.github.ldaniels528.bourne.dao

import com.github.ldaniels528.bourne.models._
import io.scalajs.npm.mongodb.ObjectID

import scala.scalajs.js

/**
  * Represents a workflow document
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait WorkflowData extends WorkflowLike {
  var _id: js.UndefOr[ObjectID] = js.native
  var name: js.UndefOr[String] = js.native
  var input: js.UndefOr[SourceLike] = js.native
  var outputs: js.UndefOr[js.Array[SourceLike]] = js.native
  var events: js.UndefOr[js.Dictionary[OperationLike]] = js.native
  var variables: js.UndefOr[js.Array[VariableLike]] = js.native
}