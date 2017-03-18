package com.github.ldaniels528.broadway.worker

import com.github.ldaniels528.broadway.models.{Job, JobStatuses}
import com.github.ldaniels528.broadway.rest.LoggerFactory
import com.github.ldaniels528.broadway.worker.devices.{DeviceFactory, InputDevice, OutputDevice}
import com.github.ldaniels528.broadway.worker.models.Workflow
import io.scalajs.nodejs.Error

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success, Try}

/**
  * Job Processor
  * @author lawrence.daniels@gmail.com
  */
class JobProcessor(config: WorkerConfig, jobs: js.Dictionary[Job])(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(getClass)
  private var active = 0

  /**
    * Runs the job processor
    */
  def run(): Unit = {
    // if no process is active, start one ...
    if (active < config.getMaxConcurrency) {
      jobs.find(_._2.status == JobStatuses.QUEUED) foreach { case (_, job) =>
        job.status = JobStatuses.RUNNING
        active += 1

        val outcome = for {
          workflow <- Workflow.load(job.workflowConfig)
          stats <- compileAndRunWorkflow(workflow)(job)
        } yield stats

        outcome onComplete {
          case Success(stats) =>
            logger.info(s"Job ${job.id}: '${job.name}' (${job.input}) completed successfully")
            logger.info(s"Job ${job.id}: $stats")
            job.status = JobStatuses.SUCCESS
            jobs.delete(job.id)

          case Failure(e) =>
            logger.error(s"Job ${job.id}: File '${job.name}' (${job.input}) failed")
            logger.error(s"Job ${job.id}: ${e.getMessage}")
            job.status = JobStatuses.FAILED
            job.message = e.getMessage
        }

        outcome onComplete { _ =>
          active -= 1
        }
      }
    }
  }

  /**
    * Compiles and executes the given workflow
    * @param workflowRaw the unverified workflow
    * @return the results of the execution
    */
  private def compileAndRunWorkflow(workflowRaw: Workflow.Unsafe)(implicit job: Job) = {
    Try(compile(workflowRaw)) match {
      case Success(Some(workflow)) => runWorkflow(workflow)
      case Success(None) => Future.failed(js.JavaScriptException(s"Failed to compile for ${job.id} (${job.name})"))
      case Failure(e) => Future.failed(e)
    }
  }

  /**
    * Compiles the unverified workflow into an object that's ready for processing
    * @param workflowRaw the unverified workflow
    * @return the fully verified workflow
    */
  private def compile(workflowRaw: Workflow.Unsafe)(implicit job: Job) = {
    workflowRaw.validate match {
      case Success(workflow) =>
        // override the input source's path
        workflow.sources.find(_.name == workflow.input) map { source =>
          logger.info(s"${job.name}: Overriding ${workflow.input} path to '${job.input}'")
          source.path = job.input
          workflow
        }
      case Failure(e) =>
        e.printStackTrace()
        logger.error(s"Invalid workflow: ${e.getMessage}")
        None
    }
  }

  /**
    * Runs the workflow
    * @param workflow the given work job
    * @return a promise of the [[Statistics statistics]]
    */
  private def runWorkflow(workflow: Workflow)(implicit job: Job) = {
    val args = for {
      input <- workflow.sources.find(_.name == workflow.input)
      inputDevice <- DeviceFactory.getInputDevice(input)
      outputs = workflow.outputs.toSeq.flatMap(output => workflow.sources.find(_.name == output))
      outputDevices = outputs.flatMap(DeviceFactory.getOutputDevice(_)) if outputs.nonEmpty
    } yield (inputDevice, outputDevices)

    args match {
      case Some((inputDevice, outputDevices)) => runETL(inputDevice, outputDevices)
      case None =>
        Future.failed(js.JavaScriptException("The process could not be initialized"))
    }
  }

  /**
    * Runs the ETL process
    * @param inputDevice   the given [[InputDevice input device]]
    * @param outputDevices the given [[OutputDevice output devices]]
    * @return a promise of the [[Statistics statistics]]
    */
  private def runETL(inputDevice: InputDevice, outputDevices: Seq[OutputDevice])(implicit job: Job) = {
    // create a new the statistics generator instance
    implicit val statsGen = new StatisticsGenerator()

    def onData(data: js.Any) {
      // update the statistics
      statsGen.update() foreach (stat => logger.info(s"${job.id}: $stat"))

      // write the data to all output devices
      outputDevices foreach (_.write(data))
    }

    def onError(error: Error) {
      statsGen.failures += 1
      logger.error(s"${job.id}: Error: ${error.message}")
    }

    def onFinish(data: js.Any): Future[Unit] = {
      // close all devices
      for {
        _ <- Future.sequence(outputDevices.map(_.flush())).map(_.sum)
        _ <- inputDevice.close()
        _ <- Future.sequence(outputDevices.map(_.close()))
      } yield ()
    }

    // start reading from the input device
    inputDevice.start(onData, onError, onFinish)
  }

}
