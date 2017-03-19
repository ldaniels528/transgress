package com.github.ldaniels528.broadway.worker

import com.github.ldaniels528.broadway.models.JobStates.JobState
import com.github.ldaniels528.broadway.models.{Job, JobStates}
import com.github.ldaniels528.broadway.rest.LoggerFactory
import com.github.ldaniels528.broadway.worker.JobProcessor.{FileWatch, _}
import com.github.ldaniels528.broadway.worker.devices.{DeviceFactory, InputDevice, OutputDevice}
import com.github.ldaniels528.broadway.worker.models.Workflow
import io.scalajs.JSON
import io.scalajs.nodejs.fs.{Fs, Stats}
import io.scalajs.nodejs.path.Path
import io.scalajs.nodejs.{Error, setTimeout}
import io.scalajs.npm.glob.{Glob, _}
import io.scalajs.npm.mkdirp.Mkdirp
import io.scalajs.util.DateHelper._
import io.scalajs.util.DurationHelper._
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.{Failure, Success, Try}

/**
  * Job Processor
  * @author lawrence.daniels@gmail.com
  */
class JobProcessor()(implicit config: WorkerConfig, ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val watching = js.Dictionary[FileWatch]()
  private val jobs = js.Dictionary[Job]()
  private var active = 0

  /**
    * Returns the collection of jobs currently being processed
    * @return the collection of [[Job job]]s
    */
  def getJobs: Seq[Job] = jobs.values.toSeq

  /**
    * Runs the job processor
    */
  def run(): Unit = {
    // search for new files
    searchForNewFiles()

    // if no process is active, start one ...
    if (active < config.getMaxConcurrency) {
      jobs.find(_._2.state == JobStates.QUEUED) foreach { case (_, job) =>
        active += 1

        val outcome = for {
          _ <- job.changeState(JobStates.RUNNING)
          workflow <- loadWorkflow(job.workflowConfig)
          stats <- compileAndRunWorkflow(workflow)(job)
          _ <- job.changeState(JobStates.SUCCESS)
        } yield stats

        outcome onComplete {
          case Success(stats) =>
            job.info(s"File '${job.input}' completed successfully")
            job.info(s"$stats")

          case Failure(e) =>
            job.error(s"File '${job.input}' failed")
            job.error(s"${e.getMessage}")
            job.changeState(JobStates.STOPPED, e.getMessage)
        }

        outcome onComplete { _ =>
          active -= 1

          // remove the job
          jobs.find(_._2.id == job.id).foreach(t => jobs.remove(t._1))
        }
      }
    }
  }

  private def searchForNewFiles(): Unit = {
    for {
      workflow <- config.workflows getOrElse js.Array()
      pattern <- workflow.patterns getOrElse js.Array()
    } {
      Glob.async(s"${config.incomingDirectory}/$pattern").future onComplete {
        case Success(files) =>
          for {
            file <- files if !watching.contains(file) && !jobs.contains(file)
          } ensureCompleteFile(workflow, file)
        case Failure(e) =>
          logger.error(s"${workflow.name}: Failed while searching for new files: ${e.getMessage}")
      }
    }
  }

  private def ensureCompleteFile(workflow: WorkflowConfig, file: String): Unit = {
    Fs.statAsync(file).future onComplete {
      case Success(stats) =>
        val watched = watching.getOrElseUpdate(file, {
          logger.info(s"${workflow.name}: Watching '$file' (${stats.mtime})...")
          new FileWatch(stats)
        })
        watched match {
          case w if w.stats.size != stats.size || w.stats.mtime.getTime() != stats.mtime.getTime() =>
            logger.info(s"${workflow.name}: '$file' has changed (size = ${stats.size - w.stats.size}, mtime = ${stats.mtime - w.stats.mtime})")
            w.stats = stats
            setTimeout(() => ensureCompleteFile(workflow, file), 15.seconds)
          case w if w.elapsedTime < 30.seconds =>
            logger.info(s"${workflow.name}: '$file' is still too young (${w.elapsedTime} msec)")
            setTimeout(() => ensureCompleteFile(workflow, file), 7.5.seconds)
          case _ =>
            queueForProcessing(workflow, file)
        }

      case Failure(e) =>
        logger.info(s"${workflow.name}: Failed to stat '$file'")
      // TODO retry up to N times?
    }
  }

  private def queueForProcessing(workflowConfig: WorkflowConfig, incomingFile: String) = {
    for {
      name <- workflowConfig.name
      workFile <- config.workFile(incomingFile)
      workflowConfigName <- workflowConfig.config
      workflowConfigPath <- config.workflow(workflowConfigName)
    } {
      val job = new Job(name = name, input = incomingFile, workflowConfig = workflowConfigPath)
      logger.info(s"$name: Created job ${job.id} (${job.name}) for file '$incomingFile'...")

      job.changeState(JobStates.QUEUED) onComplete {
        case Success(_) =>
          job.info(s"File '$workFile' is queued for processing...")
          jobs(incomingFile) = job
          watching.remove(incomingFile)
        case Failure(e) =>
          logger.error(s"Failed to move '$incomingFile' to '${config.workDirectory}': ${e.getMessage}")
        // TODO retry up to N times?
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
        val inputFilePath = ExpressionEvaluator.evaluate(job.input)

        // override the input source's path
        job.info(s"Overriding source ${workflow.input.name}'s path as '$inputFilePath'")
        workflow.input.path = inputFilePath
        Some(workflow)
      case Failure(e) =>
        job.error(s"Invalid workflow: ${e.getMessage}")
        None
    }
  }

  private def loadWorkflow(path: String)(implicit ec: ExecutionContext): Future[Workflow.Unsafe] = {
    logger.info(s"Loading workflow '$path'...")
    Fs.readFileAsync(path).future map (buf => JSON.parseAs[Workflow.Unsafe](buf.toString()))
  }

  /**
    * Runs the workflow
    * @param workflow the given work job
    * @return a promise of the [[Statistics statistics]]
    */
  private def runWorkflow(workflow: Workflow)(implicit job: Job) = {
    val args = for {
      inputDevice <- DeviceFactory.getInputDevice(workflow.input)
      outputDevices = workflow.outputs.flatMap { source =>
        source.path = ExpressionEvaluator.evaluate(source.path)
        DeviceFactory.getOutputDevice(source)
      }
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
      statsGen.totalRead += 1
      statsGen.update() foreach (stat => job.info(s"${job.id}: $stat"))

      // write the data to all output devices
      outputDevices foreach (_.write(data))
    }

    def onError(err: Error) {
      statsGen.failures += 1
      job.error(err.message)
    }

    def onFinish(data: js.Any): Future[Unit] = {
      job.info(s"Closing all devices...")
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

/**
  * JobProcessor Companion
  * @author lawrence.daniels@gmail.com
  */
object JobProcessor {
  private[this] val logger = LoggerFactory.getLogger(getClass)

  @ScalaJSDefined
  class FileWatch(var stats: Stats) extends js.Object {
    def elapsedTime: Double = js.Date.now() - stats.mtime.getTime()
  }

  /**
    * Job Enrichment
    * @param job the given [[Job job]]
    */
  final implicit class JobEnrichment(val job: Job) extends AnyVal {

    @inline
    def info(message: String): Unit = {
      logger.info(s"Job ${job.id}: $message")
    }

    @inline
    def error(message: String): Unit = {
      logger.error(s"Job ${job.id}: $message")
    }

    def changeState(state: JobState, message: String = null)(implicit config: WorkerConfig): Future[Unit] = {
      import JobStates._

      val stateFileMapping = Map[JobState, String => js.UndefOr[String]](
        QUEUED -> config.workFile,
        SUCCESS -> config.archiveFile
      )

      info(s"Moving '${job.input}' from ${job.state} to $state...")
      val task = stateFileMapping.get(state) match {
        case Some(f) =>
          val newFile = f(job.input).orDie(s"$state file path could not be determined for ${job.input}")
          val newFilePath = Path.parse(newFile).dir.orDie(s"$state directory could not be determined for ${job.input}")
          for {
            _ <- Mkdirp.async(newFilePath).future
            _ <- Fs.renameAsync(job.input, newFile).future
          } yield Some(newFile)
        case None =>
          Future.successful(None)
      }

      task map { optionalPath =>
        job.state = state
        Option(message).foreach(job.message = _)
        optionalPath.foreach(job.input = _)
      }
    }

  }

}