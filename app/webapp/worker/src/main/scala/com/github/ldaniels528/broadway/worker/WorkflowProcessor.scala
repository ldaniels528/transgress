package com.github.ldaniels528.broadway.worker

import io.scalajs.util.JsUnderOrHelper._
import com.github.ldaniels528.broadway.models.Job
import com.github.ldaniels528.broadway.rest.LoggerFactory
import com.github.ldaniels528.broadway.worker.WorkflowProcessor.CompiledWorkflow

import scala.concurrent.Future
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Workflow Processor
  * @author lawrence.daniels@gmail.com
  */
class WorkflowProcessor(config: WorkerConfig, job: Job, workflowUnsafe: WorkflowUnsafe) {
  private val logger = LoggerFactory.getLogger(getClass)

  def execute(): Unit = {
    compile() match {
      case Some(workflow) =>
        workflow.input
        Future.successful(1L)
      case None =>
        Future.successful(0L)
    }
  }

  private def compile() = {
    workflowUnsafe.toSafe match {
      case Success(workflowSafe) =>
        Option(workflowSafe)
      case Failure(e) =>
        logger.error(s"Invalid workflow: ${e.getMessage}")
        None
    }

    for {
      input <- workflowUnsafe.input.toOption
      outputs <- workflowUnsafe.outputs.toOption
      sources <- workflowUnsafe.sources.toOption.map(_.map(_.toSafe match {
        case Success(source) => source
        case Failure(e) => throw js.JavaScriptException(e)
      }))
      onError = workflowUnsafe.onError.toOption

      inputSrc <- sources.find(_.name.contains(input))
      outputSrcs = outputs.flatMap(output => sources.find(_.name.contains(output)))
    } yield CompiledWorkflow(inputSrc, outputSrcs, onError)
  }

}

/**
  * Workflow Processor Companion
  * @author lawrence.daniels@gmail.com
  */
object WorkflowProcessor {

  case class CompiledWorkflow(input: Source, outputs: Seq[Source], onError: Option[OnError])

}
