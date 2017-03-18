package com.github.ldaniels528.broadway.worker.models

import com.github.ldaniels528.broadway.rest.LoggerFactory
import io.scalajs.JSON
import io.scalajs.nodejs.fs.Fs
import io.scalajs.util.OptionHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.{Failure, Success, Try}

/**
  * Represents a Workflow
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Workflow(val input: String,
               val outputs: js.Array[String],
               val onError: Option[OnError],
               val sources: js.Array[Source]) extends js.Object

/**
  * Workflow Companion
  * @author lawrence.daniels@gmail.com
  */
object Workflow {
  private val logger = LoggerFactory.getLogger(getClass)

  def load(path: String)(implicit ec: ExecutionContext): Future[Workflow.Unsafe] = {
    logger.info(s"Loading workflow '$path'...")
    Fs.readFileAsync(path).future map (buf => JSON.parseAs[Workflow.Unsafe](buf.toString()))
  }

  /**
    * Represents a Workflow (unsafe)
    * @author lawrence.daniels@gmail.com
    */
  @js.native
  trait Unsafe extends js.Object {
    val input: js.UndefOr[String] = js.native
    val outputs: js.UndefOr[js.Array[String]] = js.native
    val onError: js.UndefOr[OnError] = js.native
    val sources: js.UndefOr[js.Array[Source.Unsafe]] = js.native
  }

  /**
    * Workflow.Unsafe Enrichment
    * @param workflow the given [[Workflow.Unsafe workflow]]
    */
  implicit class WorkflowUnsafeEnrichment(val workflow: Workflow.Unsafe) extends AnyVal {

    @inline
    def validate: Try[Workflow] = {
      Try {
        val input = workflow.input.toOption.orDie("No input specified")
        val outputs = workflow.outputs.toOption.orDie("No outputs specified")
        val sources = workflow.sources.map(_.map(_.validate match {
          case Success(v) => v
          case Failure(e) => throw js.JavaScriptException(e.getMessage)
        })).getOrElse(js.Array())
        val onError = workflow.onError.toOption
        new Workflow(input, outputs, onError, sources)
      }
    }
  }

}