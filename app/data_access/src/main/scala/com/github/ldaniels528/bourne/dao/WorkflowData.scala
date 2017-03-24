package com.github.ldaniels528.bourne.dao

import io.scalajs.npm.mongodb.ObjectID

import scala.scalajs.js

/**
  * Represents a workflow document
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait WorkflowData extends js.Object {
  var _id: js.UndefOr[ObjectID] = js.native
  var name: js.UndefOr[String] = js.native
  var outputs: js.Array[SourceData] = js.native
  var onError: Option[OnErrorData] = js.native
  var variables: js.Array[VariableData] = js.native
}

/**
  * Represents an "on error" event document
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait OnErrorData extends js.Object {
  var source: js.UndefOr[String] = js.native
}

/**
  * Represents an I/O source document
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait SourceData extends js.Object {
  var name: js.UndefOr[String] = js.native
  var path: js.UndefOr[String] = js.native
  var `type`: js.UndefOr[String] = js.native
  var format: js.UndefOr[String] = js.native
  var columnHeaders: js.UndefOr[Boolean] = js.native
  var fields: js.UndefOr[js.Array[FieldData]] = js.native
  var mongoConnect: js.UndefOr[String] = js.native
  var mongoCollection: js.UndefOr[String] = js.native
}

/**
  * Represents a field document
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait FieldData extends js.Object {
  var name: js.UndefOr[String] = js.native
  var length: js.UndefOr[Int] = js.native
}

/**
  * Represents an variable document
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait VariableData extends js.Object {
  var name: js.UndefOr[String] = js.native
}

/**
  * Workflow Data Companion
  * @author lawrence.daniels@gmail.com
  */
object WorkflowData {


}