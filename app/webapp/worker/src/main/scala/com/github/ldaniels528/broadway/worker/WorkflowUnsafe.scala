package com.github.ldaniels528.broadway.worker

import io.scalajs.util.JsUnderOrHelper._
import com.github.ldaniels528.broadway.rest.LoggerFactory
import io.scalajs.JSON
import io.scalajs.nodejs.fs.Fs
import io.scalajs.util.OptionHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success, Try}

/**
  * Represents a Workflow (unsafe)
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait WorkflowUnsafe extends js.Object {
  val input: js.UndefOr[String] = js.native
  val outputs: js.UndefOr[js.Array[String]] = js.native
  val onError: js.UndefOr[OnError] = js.native
  val sources: js.UndefOr[js.Array[SourceUnsafe]] = js.native
}

/**
  * Unsafe Workflow Companion
  * @author lawrence.daniels@gmail.com
  */
object WorkflowUnsafe {
  private val logger = LoggerFactory.getLogger(getClass)

  def load(path: String)(implicit ec: ExecutionContext): Future[WorkflowUnsafe] = {
    logger.info(s"Loading workflow '$path'...")
    Fs.readFileAsync(path).future map (buf => JSON.parseAs[WorkflowUnsafe](buf.toString()))
  }

  /**
    * Workflow Enrichment
    * @param workflow the given [[WorkflowUnsafe workflow]]
    */
  implicit class WorkflowEnrichment(val workflow: WorkflowUnsafe) extends AnyVal {

    @inline
    def toSafe: Try[Workflow] = {
      Try {
        val input = workflow.input.toOption.orDie("No input specified")
        val outputs = workflow.outputs.toOption.orDie("No outputs specified")
        val sources = workflow.sources.map(_.map(_.toSafe match {
          case Success(v) => v
          case Failure(e) => throw js.JavaScriptException(e)
        })).getOrElse(js.Array())
        val onError = workflow.onError.toOption
        new Workflow(input, outputs, onError, sources)
      }
    }
  }

}

/**
  * Represents a Source (unsafe)
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait SourceUnsafe extends js.Object {
  val name: js.UndefOr[String] = js.native
  val path: js.UndefOr[String] = js.native
  val `type`: js.UndefOr[String] = js.native
  val format: js.UndefOr[String] = js.native
  val columnHeaders: js.UndefOr[Boolean] = js.native
}

/**
  * Represents a Source (unsafe)
  * @author lawrence.daniels@gmail.com
  */
object SourceUnsafe {

  /**
    * Workflow Enrichment
    * @param source the given [[WorkflowUnsafe workflow]]
    */
  implicit class SourceUnsafeEnrichment(val source: SourceUnsafe) extends AnyVal {

    @inline
    def toSafe: Try[Source] = {
      Try {
        val name = source.name.toOption.orDie("No name specified")
        val path = source.path.toOption.orDie("No path specified")
        val `type` = source.`type`.toOption.orDie("No type specified")
        val format = source.format.toOption.orDie("No format specified")
        val columnHeaders = source.columnHeaders.isTrue
        new Source(name, path, `type`, format, columnHeaders)
      }
    }
  }

}

@js.native
trait OnError extends js.Object {
  val source: js.UndefOr[String] = js.native
}

