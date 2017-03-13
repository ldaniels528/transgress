package com.github.ldaniels528.broadway.worker

import com.github.ldaniels528.broadway.models.{Job, JobStatuses}
import com.github.ldaniels528.broadway.rest.LoggerFactory
import com.github.ldaniels528.broadway.rest.ProcessHelper._
import com.github.ldaniels528.broadway.rest.StringHelper._
import com.github.ldaniels528.broadway.worker.routes.{JobRoutes, NextFunction}
import io.scalajs.nodejs.fs.Fs
import io.scalajs.nodejs.{process, setInterval}
import io.scalajs.npm.bodyparser.{BodyParser, UrlEncodedBodyOptions}
import io.scalajs.npm.express._
import io.scalajs.npm.glob._
import io.scalajs.npm.mongodb.{Db, MongoClient}
import io.scalajs.util.DurationHelper._

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success}

/**
  * Broadway Worker
  * @author lawrence.daniels@gmail.com
  */
object BroadwayWorker extends js.JSApp {
  private val logger = LoggerFactory.getLogger(getClass)
  private val jobs = js.Dictionary[Job]()

  @JSExport
  override def main(): Unit = {
    logger.info("Starting the Broadway Worker...")

    // determine the port to listen on
    val startTime = js.Date.now()

    // setup mongodb connection
    logger.info("Loading MongoDB module...")
    val dbConnect = process.dbConnect getOrElse "mongodb://localhost:27017/broadway"
    logger.info("Connecting to database '%s'...", dbConnect)
    MongoClient.connectAsync(dbConnect).toFuture onComplete {
      case Success(db) => start(startTime)(db)
      case Failure(e) =>
        logger.error(s"Error connecting to database: ${e.getMessage}")
    }
  }

  /**
    * Starts the worker
    * @param startTime the give start time
    * @param db        the given [[Db database]]
    */
  def start(startTime: Double)(implicit db: Db) {
    // load the worker config
    implicit val config = loadWorkerConfig()

    // setup the application
    val port = process.port getOrElse "1337"
    val app = configureApplication()
    app.listen(port, () => logger.info("Server now listening on port %s [%d msec]", port, js.Date.now() - startTime))

    // setup the file watchers
    setInterval(() => searchForNewFiles(), 5.seconds)

    // start the job processor
    val jobProcessor = new JobProcessor(config, jobs)
    setInterval(() => jobProcessor.execute(), 5.seconds)

    // handle any uncaught exceptions
    process.onUncaughtException { err =>
      logger.error("An uncaught exception was fired:")
      logger.error(err.stack)
    }
  }

  private def loadWorkerConfig()(implicit db: Db) = {
    WorkerConfig(baseDirectory = "/Users/ldaniels/broadway")
  }

  private def configureApplication()(implicit db: Db): Application = {
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
    logger.info("Setting up all other routes...")
    new JobRoutes(app, jobs, db)
    app
  }

  private def searchForNewFiles()(implicit db: Db, config: WorkerConfig): Unit = {
    Glob.async(s"${config.incomingDirectory}/*").future onComplete {
      case Success(files) =>
        if (files.nonEmpty) {
          for {
            file <- files if !jobs.contains(file)
            workFile <- config.workFile(file)
          } queueForProcessing(file, workFile)
        }
      case Failure(e) =>
        logger.error(s"Failed while searching for new files: ${e.getMessage}")
    }
  }

  private def queueForProcessing(file: String, workFile: String)(implicit db: Db, config: WorkerConfig) = {
    logger.info(s"Moving '$file' to '$workFile'")
    jobs(file) = new Job(name = "LoadListingActivity", input = workFile, status = JobStatuses.STAGED)
    Fs.renameAsync(file, workFile).future onComplete {
      case Success(_) =>
        jobs(file).job.status = JobStatuses.QUEUED
        logger.info(s"File '$workFile' is ready for processing...")
      case Failure(e) =>
        logger.error(s"Failed to move '$file' to '${config.workDirectory}'")
    }
  }

}
