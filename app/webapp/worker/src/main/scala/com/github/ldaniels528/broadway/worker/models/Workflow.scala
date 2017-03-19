package com.github.ldaniels528.broadway.worker.models

import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.{Failure, Success, Try}

/**
  * Represents a Workflow
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Workflow(val input: Source,
               val outputs: js.Array[Source],
               val onError: Option[OnError],
               val variables: js.Array[Variable]) extends js.Object

/**
  * Workflow Companion
  * @author lawrence.daniels@gmail.com
  */
object Workflow {

  /**
    * Represents a Workflow (unsafe)
    * @author lawrence.daniels@gmail.com
    */
  @js.native
  trait Unsafe extends js.Object {
    val input: js.UndefOr[Source.Unsafe] = js.native
    val outputs: js.UndefOr[js.Array[Source.Unsafe]] = js.native
    val onError: js.UndefOr[OnError] = js.native
    val variables: js.UndefOr[js.Array[Variable.Unsafe]] = js.native
  }

  /**
    * Workflow.Unsafe Enrichment
    * @param workflow the given [[Workflow.Unsafe workflow]]
    */
  implicit class WorkflowUnsafeEnrichment(val workflow: Workflow.Unsafe) extends AnyVal {

    @inline
    def validate: Try[Workflow] = {
      Try {
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
        val onError = workflow.onError.toOption
        new Workflow(input, outputs, onError, variables)
      }
    }
  }

}