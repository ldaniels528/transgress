package com.github.ldaniels528.bourne.worker

import com.github.ldaniels528.bourne.models.JobStates.JobState
import com.github.ldaniels528.bourne.models.{JobStates, StatisticsLike, WorkflowLike}
import com.github.ldaniels528.bourne.rest.{Job, LoggerFactory}
import com.github.ldaniels528.bourne.worker.JobProcessor.{FileWatch, _}
import com.github.ldaniels528.bourne.worker.devices._
import com.github.ldaniels528.bourne.worker.models.Workflow
import com.github.ldaniels528.bourne.worker.models.Workflow._
import com.github.ldaniels528.bourne.worker.rest.{JobClient, WorkflowClient}
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
class JobProcessor()(implicit config: WorkerConfig, jobDAO: JobClient, workflowDAO: WorkflowClient, ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val watching = js.Dictionary[FileWatch]()
  private var active = 0

  /**
    * Runs the job processor
    */
  def run(): Unit = {
    // search for new files
    searchForNewFiles()

    // if no process is active, start one ...
    if (active < config.getMaxConcurrency) {
      jobDAO.getNextJob() foreach {
        case Some(job) =>
          active += 1

          val outcome = for {
            _ <- job.changeState(JobStates.QUEUED)
            workflow <- loadWorkflow(job)
            _ <- job.changeState(JobStates.RUNNING)
            stats <- compileAndRunWorkflow(workflow)(job)
            _ <- job.changeState(JobStates.SUCCESS)
          } yield stats

          outcome onComplete {
            case Success(stats) =>
              job.info(s"File '${job.input}' completed successfully")
              job.info(s"$stats")

            case Failure(e) =>
              job.error(s"File '${job.input}' failed")
              job.error(s"${e.getMessage}", e)
              job.changeState(JobStates.STOPPED, e.getMessage)
          }

          outcome onComplete { _ =>
            active -= 1

            // remove the job
            jobDAO.untrackJob(job)
          }
        case None =>
        // No jobs found
      }
    }
  }

  private def searchForNewFiles(): Unit = {
    for {
      trigger <- config.triggers getOrElse js.Array()
      pattern <- trigger.patterns getOrElse js.Array()
    } {
      Glob.async(s"${config.incomingDirectory}/$pattern").future onComplete {
        case Success(files) =>
          for {
            file <- files if !watching.contains(file) && !jobDAO.isTracked(file)
          } ensureCompleteFile(trigger, file)
        case Failure(e) =>
          logger.error(s"${trigger.name}: Failed while searching for new files: ${e.getMessage}")
      }
    }
  }

  private def ensureCompleteFile(trigger: Trigger, file: String): Unit = {
    Fs.statAsync(file).future onComplete {
      case Success(stats) =>
        val watched = watching.getOrElseUpdate(file, {
          logger.info(s"${trigger.name}: Watching '$file' (${stats.mtime})...")
          new FileWatch(stats)
        })
        watched match {
          case w if w.stats.size != stats.size || w.stats.mtime.getTime() != stats.mtime.getTime() =>
            logger.info(s"${trigger.name}: '$file' has changed (size = ${stats.size - w.stats.size}, mtime = ${stats.mtime - w.stats.mtime})")
            w.stats = stats
            setTimeout(() => ensureCompleteFile(trigger, file), 15.seconds)
          case w if w.elapsedTime < 30.seconds =>
            logger.info(s"${trigger.name}: '$file' is still too young (${w.elapsedTime} msec)")
            setTimeout(() => ensureCompleteFile(trigger, file), 7.5.seconds)
          case _ =>
            queueForProcessing(trigger, file)
        }

      case Failure(e) =>
        logger.info(s"${trigger.name}: Failed to stat '$file'")
      // TODO retry up to N times?
    }
  }

  private def queueForProcessing(trigger: Trigger, incomingFile: String) = {
    for {
      name <- trigger.name
      workFile <- config.workFile(incomingFile)
      workflowName <- trigger.workflowName
      workflowPath <- config.workflow(workflowName)
    } {
      logger.info(s"$name: Queuing '$incomingFile' (workflow $workflowPath)")
      val outcome = for {
        job <- jobDAO.createJob(new Job(_id = js.undefined, name = name, input = incomingFile, workflowConfig = workflowPath, state = JobStates.NEW))
      //_ <- job.changeState(JobStates.QUEUED)
      } yield job

      outcome onComplete {
        case Success(job) =>
          logger.info(s"$name: Created job ${job._id} (${job.name}) for file '$incomingFile'...")
          job.info(s"File '$workFile' is queued for processing...")
          jobDAO.trackJob(incomingFile, job)
          watching.remove(incomingFile)
        case Failure(e) =>
          logger.error(s"Failed to move '$incomingFile' to '${config.workDirectory}': ${e.getMessage}", e)
        // TODO retry up to N times?
      }
    }
  }

  /**
    * Compiles and executes the given workflow
    * @param workflowRaw the unverified workflow
    * @return the results of the execution
    */
  private def compileAndRunWorkflow(workflowRaw: WorkflowLike)(implicit job: Job) = {
    Try(compile(workflowRaw)) match {
      case Success(Some(workflow)) => runWorkflow(workflow)
      case Success(None) => Future.failed(js.JavaScriptException(s"Failed to compile for ${job._id} (${job.name})"))
      case Failure(e) => Future.failed(e)
    }
  }

  /**
    * Compiles the unverified workflow into an object that's ready for processing
    * @param workflowRaw the unverified workflow
    * @return the fully verified workflow
    */
  private def compile(workflowRaw: WorkflowLike)(implicit job: Job) = {
    workflowRaw.validate match {
      case Success(workflow) =>
        val input = job.input.orDie(s"No input source defined for job #${job._id.orNull}")
        val inputFilePath = ExpressionEvaluator.evaluate(input)

        // override the input source's path
        job.info(s"Overriding source ${workflow.input.name}'s path as '$inputFilePath'")
        workflow.input.path = inputFilePath
        Some(workflow)
      case Failure(e) =>
        job.error(s"Invalid workflow: ${e.getMessage}", e)
        None
    }
  }

  private def loadWorkflow(job: Job)(implicit ec: ExecutionContext): Future[WorkflowLike] = {
    job.workflowConfig.toOption match {
      case Some(workflowRef) =>
        job.info(s"Loaded workflow '$workflowRef' from ${config.master}...")
        workflowDAO.findByName(workflowRef) flatMap {
          case Some(workflow) => Future.successful(workflow)
          case None =>
            logger.info(s"Loading workflow '$workflowRef'...")
            Fs.readFileAsync(workflowRef).future map (buf => JSON.parseAs[WorkflowLike](buf.toString()))
        }
      case None =>
        Future.failed(js.JavaScriptException(s"No workflow specified for job ${job._id.orNull}"))
    }
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
      statsGen.update() foreach { statistics =>
        job.info(statistics.toString)
        job.statistics = statistics.toModel
        for {
          _id <- job._id
          statistics <- job.statistics
        } jobDAO.updateStatistics(_id, statistics)
      }

      // write the data to all output devices
      outputDevices foreach (_.write(data))
    }

    def onError(err: Error) {
      statsGen.failures += 1
      job.error(err.message, err)
    }

    def onFinish(data: js.Any): Future[Unit] = {
      job.info(s"Closing all devices...")
      for {
        _ <- jobDAO.updateJob(job)
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
      logger.info(s"Job ${job._id}: $message")
    }

    @inline
    def error(message: String, cause: Throwable = null): Unit = {
      logger.error(s"Job ${job._id}: $message")
      if (cause != null) {
        cause.printStackTrace()
      }
    }

    def changeState(state: JobState, message: String = null)(implicit config: WorkerConfig, jobDAO: JobClient): Future[Unit] = {
      import JobStates._

      val stateFileMapping = Map[JobState, String => js.UndefOr[String]](
        QUEUED -> config.workFile,
        SUCCESS -> config.archiveFile
      )

      // update the state
      val task0 = jobDAO.changeState(job._id.orDie("No Job ID"), state)

      // display the message if set
      if (message != null) {
        logger.error(message)
      }

      info(s"Moving '${job.input}' from ${job.state} to $state...")
      val task = stateFileMapping.get(state) match {
        case Some(f) =>
          val input = job.input.orDie(s"No input source defined for job #${job._id.orNull}")
          val newFile = f(input).orDie(s"$state file path could not be determined for $input")
          val newFilePath = Path.parse(newFile).dir.orDie(s"$state directory could not be determined for $input")
          for {
            _ <- task0
            _ <- Mkdirp.async(newFilePath).future
            _ <- Fs.renameAsync(input, newFile).future
          } yield Some(newFile)
        case None =>
          task0.map(_ => None)
      }

      task map { optionalPath =>
        job.state = state
        Option(message).foreach(job.message = _)
        optionalPath.foreach(job.input = _)
      }
    }

  }

  final implicit class JobStatisticsEnrichment(val statistics: Statistics) extends AnyVal {

    @inline
    def toModel = new StatisticsLike(
      totalInserted = statistics.totalInserted.toInt,
      bytesRead = statistics.bytesRead.toInt,
      bytesPerSecond = statistics.bytesPerSecond,
      recordsDelta = statistics.recordsDelta.toInt,
      recordsPerSecond = statistics.recordsPerSecond,
      pctComplete = statistics.complete_%,
      completionTime = statistics.completionTime
    )

  }

}