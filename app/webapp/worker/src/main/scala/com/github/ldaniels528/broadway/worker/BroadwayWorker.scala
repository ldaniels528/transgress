package com.github.ldaniels528.broadway.worker

import com.github.ldaniels528.broadway.rest.LoggerFactory
import com.github.ldaniels528.broadway.rest.ProcessHelper._
import com.github.ldaniels528.broadway.rest.StringHelper._
import com.github.ldaniels528.broadway.worker.routes.{JobRoutes, NextFunction}
import io.scalajs.nodejs.fs.Fs
import io.scalajs.nodejs.{process, setInterval}
import io.scalajs.npm.bodyparser.{BodyParser, UrlEncodedBodyOptions}
import io.scalajs.npm.express._
import io.scalajs.npm.mongodb.{Db, MongoClient}
import io.scalajs.util.DurationHelper._

import scala.concurrent.Future
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
    implicit val config = WorkerConfig.load()

    ensureLocalDirectories() onComplete {
      case Success(results) =>
        // were directories created?
        results foreach { case (directory, exists) =>
          if (!exists) logger.info(s"Created directory '$directory'...")
        }

        // start the job processor
        val jobProcessor = new JobProcessor()
        setInterval(() => jobProcessor.run(), 5.seconds)

        // setup the application
        val port = process.port getOrElse "1337"
        val app = configureApplication(jobProcessor)
        app.listen(port, () => logger.info("Server now listening on port %s [%d msec]", port, js.Date.now() - startTime))

        // handle any uncaught exceptions
        process.onUncaughtException { err =>
          logger.error("An uncaught exception was fired:")
          logger.error(err.stack)
        }
      case Failure(e) =>
        logger.error(s"Failed to initialize processing directories; ${e.getMessage}")
    }
  }

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

  private def ensureLocalDirectory(directory: String) = {
    for {
      exists <- Fs.existsAsync(directory).future
      _ <- if (!exists) Fs.mkdirAsync(directory).future else Future.successful({})
    } yield (directory, exists)
  }

  private def configureApplication(jobProcessor: JobProcessor)(implicit db: Db): Application = {
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
    new JobRoutes(app, jobProcessor, db)
    app
  }

}
