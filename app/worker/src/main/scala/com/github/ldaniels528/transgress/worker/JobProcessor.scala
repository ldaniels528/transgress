package com.github.ldaniels528.transgress.worker

import com.github.ldaniels528.transgress.models.JobStates.JobState
import com.github.ldaniels528.transgress.models.{JobStates, StatisticsLike, WorkflowLike}
import com.github.ldaniels528.transgress.rest._
import com.github.ldaniels528.transgress.worker.JobProcessor._
import com.github.ldaniels528.transgress.worker.devices._
import com.github.ldaniels528.transgress.worker.devices.formats.DataFormatFactory
import com.github.ldaniels528.transgress.worker.devices.input.InputDevice
import com.github.ldaniels528.transgress.worker.devices.output.OutputDevice
import com.github.ldaniels528.transgress.worker.devices.sources.{FileSource, Source}
import com.github.ldaniels528.transgress.worker.models.Workflow._
import com.github.ldaniels528.transgress.worker.models.{Statistics, Workflow}
import com.github.ldaniels528.transgress.{CpuMonitor, LoggerFactory}
import io.scalajs.JSON
import io.scalajs.nodejs.fs.Fs
import io.scalajs.nodejs.path.Path
import io.scalajs.nodejs.{Error, setInterval, setTimeout}
import io.scalajs.npm.mkdirp.Mkdirp
import io.scalajs.util.DurationHelper._
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.OptionHelper._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success, Try}

/**
  * Job Processor
  * @author lawrence.daniels@gmail.com
  */
class JobProcessor(slave: Slave)(implicit config: WorkerConfig, jobDAO: JobClient, slaveDAO: SlaveClient, workflowDAO: WorkflowClient, ec: ExecutionContext)
  extends JobFileTracking {
  private val logger = LoggerFactory.getLogger(getClass)
  private var cpuLoad: Double = _
  private var active = 0

  // setup CPU load/idle monitoring
  private val cpuInterval = setInterval(() => CpuMonitor.computeLoad().future foreach (this.cpuLoad = _), 1.seconds)

  /**
    * Runs the job processor
    */
  def run(): Unit = {
    // if no process is active, start one ...
    if (active < config.getMaxConcurrency) {
      slave._id foreach { slaveID =>
        jobDAO.getNextJob(slaveID) foreach {
          case Some(job) =>
            active += 1
            slave.concurrency = active
            val outcome = for {
              _ <- slaveDAO.upsertSlave(slave)
              _ <- prepareJobForProcessing(job)
            } yield ()

            outcome onComplete { _ =>
              active -= 1
              slave.concurrency = active
              slaveDAO.upsertSlave(slave)

              // remove the job
              untrackJob(job)
            }

            // are there more files available?
            setTimeout(() => run(), 5.seconds)

          case None =>
          // No jobs found
        }
      }
    }
  }

  /**
    * Shuts down the processor
    */
  def shutdown(): Unit = {
    cpuInterval.clear()
  }

  private def prepareJobForProcessing(job: Job) = {
    val outcome = for {
      _ <- job.changeState(JobStates.QUEUED)
      workflow <- loadWorkflow(job)
      _ <- job.changeState(JobStates.RUNNING)
      statistics <- compileAndRunWorkflow(workflow)(job)
      _ <- job.changeState(JobStates.SUCCESS)
    } yield statistics

    outcome onComplete {
      case Success(stats) =>
        job.info(s"File '${job.input}' completed successfully")
        job.info(s"$stats")

      case Failure(e) =>
        job.error(s"File '${job.input}' failed")
        job.error(s"${e.getMessage}", e)
        job.changeState(JobStates.STOPPED, e.getMessage)
    }
    outcome
  }

  /**
    * Compiles and executes the given workflow
    * @param workflowRaw the unverified workflow
    * @return the results of the execution
    */
  private def compileAndRunWorkflow(workflowRaw: WorkflowLike)(implicit job: Job) = {
    compile(workflowRaw) match {
      case Some(workflow) => runWorkflow(workflow)
      case None => Future.failed(js.JavaScriptException(s"Failed to compile for ${job._id} (${job.name})"))
    }
  }

  /**
    * Compiles the unverified workflow into an object that's ready for processing
    * @param workflowRaw the unverified workflow
    * @return the fully verified workflow
    */
  private def compile(workflowRaw: WorkflowLike)(implicit job: Job): Option[Workflow] = {
    workflowRaw.validate match {
      case Success(workflow) =>
        val input = job.input.orDie(s"No input source defined for job #${job._id.orNull}")
        val inputFilePath = ExpressionEvaluator.evaluate(input)

        // override the input source's path
        job.info(s"Overriding source ${workflow.input.name}'s path as '$inputFilePath'")
        workflow.input match {
          case f: FileSource => f.path = inputFilePath
          case _ =>
            logger.warn(s"Input source type ${workflow.input.`type`} cannot be overridden")
        }
        Some(workflow)
      case Failure(e) =>
        job.error(s"Invalid workflow: ${e.getMessage}", e)
        None
    }
  }

  private def loadWorkflow(job: Job)(implicit ec: ExecutionContext): Future[WorkflowLike] = {
    val results = for {
      workflowName <- job.workflowName
      workflowPath <- config.workflowFile(workflowName)
    } yield (workflowName, workflowPath)

    results.toOption match {
      case Some((workflowName, workflowPath)) =>
        job.info(s"Loading workflow '$workflowName' from ${config.master}...")
        workflowDAO.findByName(workflowName) flatMap {
          case Some(workflow) => Future.successful(workflow)
          case None =>
            logger.info(s"Loading workflow file '$workflowPath'...")
            Fs.readFileAsync(workflowPath).future map (buf => JSON.parseAs[WorkflowLike](buf.toString))
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
  private def runWorkflow(workflow: Workflow)(implicit job: Job): Future[Statistics] = {

    def attemptDataFormat(source: Source) = Try {
      DataFormatFactory.getFormat(source.format)
        .orDie(s"Unhandled data format '${source.format}' for source ${source.name}")
    }

    def attemptInputDevice(source: Source) = Try {
      DeviceFactory.getInputDevice(source)
        .orDie(s"Unhandled source type '${source.`type`}' for input source ${source.name}")
    }

    def attemptOutputDevice(source: Source) = Try {
      DeviceFactory.getOutputDevice(source)
        .orDie(s"Unhandled source type '${source.`type`}' for output source ${source.name}")
    }

    val args = for {
      inputFormat <- attemptDataFormat(workflow.input)
      inputDevice <- attemptInputDevice(workflow.input)
      outputDeviceAttempts = workflow.outputs map { source =>
        source match {
          case f: FileSource => f.path = ExpressionEvaluator.evaluate(source.path)
          case _ =>
            logger.warn(s"Output source type ${workflow.input.`type`} cannot be overridden")
        }
        val outputDevice = for {
          format <- attemptDataFormat(source)
          device <- attemptOutputDevice(source)
        } yield device
        source.name -> outputDevice
      }
    } yield (inputDevice, outputDeviceAttempts)

    args match {
      case Success((inputDevice, outputDeviceAttempts)) =>
        val outputDevices = outputDeviceAttempts map {
          case (_, Success(device)) => device
          case (name, Failure(e)) =>
            throw js.JavaScriptException(s"Failed to initialize device '$name'", e)
        }
        runETL(inputDevice, outputDevices)
      case Failure(e) =>
        Future.failed(js.JavaScriptException("The process could not be initialized", e))
    }
  }

  /**
    * Runs the ETL process
    * @param inputDevice   the given [[InputDevice input device]]
    * @param outputDevices the given [[OutputDevice output devices]]
    * @return a promise of the [[Statistics statistics]]
    */
  private def runETL(inputDevice: InputDevice, outputDevices: Seq[OutputDevice])(implicit job: Job): Future[Statistics] = {
    // setup statistics generation (every 5 seconds)
    implicit val statsGen = new StatisticsGenerator()
    val interval = setInterval(() => updateStatistics(), 5.seconds)

    registerJobController(job, new JobControlSupport {
      override def pause(): Future[Boolean] = inputDevice.pause()

      override def resume(): Future[Boolean] = inputDevice.resume()

      override def stop(): Future[Boolean] = inputDevice.stop()
    })

    // start reading from the input device
    inputDevice.start(new JobEventHandler {
      override def onData(data: js.Any): Unit = {
        statsGen.totalRead += 1
        outputDevices foreach (_.write(data)(this))
      }

      override def onError(error: Error): Unit = {
        statsGen.failures += 1
        job.error(JSON.stringify(error, null, 4), error)
      }

      override def onFinish(data: js.Any): Future[Unit] = {
        job.info("Closing all devices...")
        interval.clear()
        for {
          _ <- Future.sequence(outputDevices.map(_.flush()(this))).map(_.sum)
          _ <- inputDevice.close()
          _ <- Future.sequence(outputDevices.map(_.close()))
        } yield updateStatistics()
      }
    })
  }

  /**
    * Updates the statistics for the underlying devices corresponding to the given job
    * @param job      the given [[Job job]]
    * @param statsGen the given [[StatisticsGenerator statistics generator]]
    */
  private def updateStatistics()(implicit job: Job, statsGen: StatisticsGenerator) = {
    val statistics = statsGen.update()
    job.info(statistics.toString)
    job._id.foreach(jobDAO.updateStatistics(_, statistics.toModel(cpuLoad)))
  }

}

/**
  * JobProcessor Companion
  * @author lawrence.daniels@gmail.com
  */
object JobProcessor {
  private[this] val logger = LoggerFactory.getLogger(getClass)

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
      if (message != null) logger.error(message)

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
    def toModel(cpuLoad: Double) = new StatisticsLike(
      cpuLoad = cpuLoad,
      totalInserted = statistics.totalInserted,
      bytesRead = statistics.bytesRead,
      bytesPerSecond = statistics.bytesPerSecond,
      recordsDelta = statistics.recordsDelta,
      recordsPerSecond = statistics.recordsPerSecond,
      pctComplete = statistics.pctComplete,
      completionTime = statistics.completionTime
    )

  }

}