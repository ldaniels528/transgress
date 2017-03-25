package com.github.ldaniels528.bourne.worker
package models

import com.github.ldaniels528.bourne.models.{OperationLike, WorkflowLike}
import com.github.ldaniels528.bourne.worker.models.Source._
import com.github.ldaniels528.bourne.worker.models.Variable._
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.{Failure, Success, Try}

/**
  * Represents a Workflow
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Workflow(val name: String,
               val input: Source,
               val outputs: js.Array[Source],
               val events: js.Dictionary[OperationLike],
               val variables: js.Array[Variable]) extends js.Object

/**
  * Workflow Companion
  * @author lawrence.daniels@gmail.com
  */
object Workflow {

  /**
    * WorkflowLike Enrichment
    * @param workflow the given [[WorkflowLike workflow]]
    */
  implicit class WorkflowLikeEnrichment(val workflow: WorkflowLike) extends AnyVal {

    @inline
    def validate: Try[Workflow] = {
      Try {
        val name = workflow.name.orDie("'name' is a required field")
        val input = workflow.input.map(_.validate match {
          case Success(v) => v
          case Failure(e) => throw js.JavaScriptException(e.getMessage)
        }).orDie("No input specified")
        val outputs = workflow.outputs.map(_.map(_.validate match {
          case Success(v) => v
          case Failure(e) => throw js.JavaScriptException(e.getMessage)
        })).orDie("No outputs specified")
        val variables = workflow.variables.map(_.map(_.validate match {
          case Success(v) => v
          case Failure(e) => throw js.JavaScriptException(e.getMessage)
        })).getOrElse(js.Array())
        val events = workflow.events.getOrElse(js.Dictionary())
        new Workflow(name, input, outputs, events, variables)
      }
    }
  }

}