package com.github.ldaniels528.broadway.server.dao

import io.scalajs.npm.mongoose._

import scala.scalajs.js

/**
  * Work Flows
  * @author lawrence.daniels@gmail.com
  */
object WorkFlows {

  val workflowSchema = {
    import Mongoose.Schema.Types._
    Schema[js.Dictionary[js.Any]](
      "id" -> SchemaField(`type` = String),
      "name" -> SchemaField(`type` = String)
    )
  }

  // define the model
  val Workflows = Mongoose.model("workflows", workflowSchema)

}
