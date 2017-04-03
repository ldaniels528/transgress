package com.github.ldaniels528.bourne.worker

import com.github.ldaniels528.bourne.AppConstants._
import com.github.ldaniels528.bourne.LoggerFactory
import com.github.ldaniels528.bourne.EnvironmentHelper._
import com.github.ldaniels528.bourne.StringHelper._
import com.github.ldaniels528.bourne.rest._
import com.github.ldaniels528.bourne.worker.routes.{NextFunction, WorkerRoutes}
import io.scalajs.JSON
import io.scalajs.nodejs.fs.Fs
import io.scalajs.nodejs.{process, setInterval}
import io.scalajs.npm.bodyparser.{BodyParser, UrlEncodedBodyOptions}
import io.scalajs.npm.express._
import io.scalajs.npm.ip.IP
import io.scalajs.util.DurationHelper._
import io.scalajs.util.OptionHelper._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success}

/**
  * Transgress Worker
  * @author lawrence.daniels@gmail.com
  */
object Worker extends js.JSApp {
  private val logger = LoggerFactory.getLogger(getClass)

  @JSExport
  override def main(): Unit = run()

  /**
    * Runs the worker application
    */
  def run(): Unit = {
    logger.info(f"Starting the Transgress Worker v$Version%.1f...")

     // capture the start time
    val startTime = js.Date.now()

    // load the worker config
    val configDirectory = process.homeDirectory orDie s"Environment variable $BOURNE_HOME is not defined"
    implicit val config = WorkerConfig.load(configDirectory)

    // determine the host and listen port
    val host = IP.address()
    val port = process.port getOrElse "1337"

    // initialize the job & workflow clients
    val master = config.master.getOrElse(process.master getOrElse "localhost:9000")
    implicit val jobClient = new JobClient(master)
    implicit val slaveClient = new SlaveClient(master)
    implicit val workflowClient = new WorkflowClient(master)

    // ensure the local processing directories exist
    val outcome = for {
      results <- ensureLocalDirectories()
      slave <- registerAsSlave(host, port)
      _ <- downloadWorkflows()
    } yield (slave, results)

    outcome onComplete {
      case Success((slave, results)) =>
        logger.info(s"slave = ${JSON.stringify(slave)}")

        // were directories created?
        results foreach { case (directory, exists) =>
          if (!exists) logger.info(s"Created directory '$directory'...")
        }

        // start the job processor
        val jobProcessor = new JobProcessor(slave)
        setInterval(() => jobProcessor.run(), 30.seconds)

        // setup the application
        val app = configureApplication(jobProcessor)
        app.listen(port, () => logger.info("Server now listening on port %s [%d msec]", port, js.Date.now() - startTime))

        // handle any uncaught exceptions
        process.onUncaughtException { err =>
          logger.error("An uncaught exception was fired:", err.stack)
          logger.error(err.stack)
        }
      case Failure(e) =>
        logger.error(s"Failed to initialize the worker: ${e.getMessage}")
    }
  }

  /**
    * Configures the application
    * @param jobProcessor the [[JobProcessor job processor]]
    * @return the [[Application application]]
    */
  private def configureApplication(jobProcessor: JobProcessor)(implicit jobClient: JobClient): Application = {
    logger.info("Loading Express modules...")
    implicit val app = Express()

    // setup the body parsers
    logger.info("Loading Body Parser...")
    app.use(BodyParser.json())
      .use(BodyParser.urlencoded(new UrlEncodedBodyOptions(extended = true)))

    // disable caching
    app.disable("etag")

    // setup logging of the request - response cycles
    app.use((request: Request, response: Response, next: NextFunction) => {
      val startTime = js.Date.now()
      next()
      response.onFinish(() => {
        val elapsedTime = js.Date.now() - startTime
        val query = if (request.query.nonEmpty) (request.query map { case (k, v) => s"$k=$v" } mkString ",").limitTo(120) else "..."
        logger.info("[node] application - %s %s (%s) ~> %d [%d ms]", request.method, request.path, query, response.statusCode, elapsedTime)
      })
    })

    // setup all other routes
    logger.info("Setting up the worker routes...")
    new WorkerRoutes(app, jobProcessor)
    app
  }

  private def downloadWorkflows()(implicit config: WorkerConfig, workflowClient: WorkflowClient) = {
    logger.info("Downloading workflows from master...")
    workflowClient.findAll() flatMap { workflows =>
      Future.sequence {
        for {
          workflow <- workflows.toList
          name <- workflow.name.toList
          workflowPath <- config.workflowFile(name).toList
        } yield {
          logger.info(s"Downloading workflow '$name' ($workflowPath) from master...")
          Fs.writeFileAsync(workflowPath, JSON.stringify(workflow, null, 4)).future
        }
      }
    }
  }

  private def registerAsSlave(host: String, port: String)(implicit config: WorkerConfig, slaveClient: SlaveClient) = {
    logger.info("Registering as a slave to master...")
    slaveClient.upsertSlave(new Slave(
      host = host,
      port = port,
      maxConcurrency = config.getMaxConcurrency,
      concurrency = 0
    ))
  }

  /**
    * Ensures all local processing directories exist
    * @param config the given [[WorkerConfig worker configuration]]
    * @return the promise of a list of a tuple including a directory and whether it existed on startup
    */
  private def ensureLocalDirectories()(implicit config: WorkerConfig) = {
    config.baseDirectory.toOption match {
      case Some(baseDirectory) =>
        logger.info(s"Ensuring the existence of processing sub-directories under '$baseDirectory'...")
        val directories = Seq(config.incomingDirectory, config.workDirectory, config.archiveDirectory, config.workflowDirectory)
        for {
          result <- ensureLocalDirectory(baseDirectory)
          results <- Future.sequence(directories map ensureLocalDirectory)
        } yield result :: results.toList
      case None =>
        Future.successful(Nil)
    }
  }

  /**
    * Ensures the existence of a specific directory
    * @param directory the directory to check
    * @return the promise of a tuple including the directory and whether it existed on startup
    */
  private def ensureLocalDirectory(directory: String) = {
    for {
      exists <- Fs.existsAsync(directory).future
      _ <- if (!exists) Fs.mkdirAsync(directory).future else Future.successful({
      })
    } yield (directory, exists)
  }

}
