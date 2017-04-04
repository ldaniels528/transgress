package com.github.ldaniels528.transgress.watcher

import com.github.ldaniels528.transgress.AppConstants.Version
import com.github.ldaniels528.transgress.EnvironmentHelper._
import com.github.ldaniels528.transgress.LoggerFactory
import com.github.ldaniels528.transgress.rest.JobClient
import io.scalajs.nodejs.{process, setInterval}
import io.scalajs.util.DurationHelper._
import io.scalajs.util.OptionHelper._

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

/**
  * Transgress Watcher
  * @author lawrence.daniels@gmail.com
  */
object Watcher extends js.JSApp {
  private val logger = LoggerFactory.getLogger(getClass)

  @JSExport
  override def main(): Unit = run()

  /**
    * Runs the worker application
    */
  def run(): Unit = {
    logger.info(f"Starting the Transgress Watcher v$Version%.1f...")

    // load the watcher config
    val configDirectory = process.homeDirectory orDie s"Environment variable $TRANSGRESS_HOME is not defined"
    implicit val config = WatcherConfig.load(configDirectory)

    // initialize the job & workflow clients
    val master = config.master.getOrElse(process.master getOrElse "localhost:9000")
    implicit val jobClient = new JobClient(master)

    // start the feed processor
    val feedProcessor = new FeedProcessor(config)
    setInterval(() => feedProcessor.searchForNewFiles(), 5.seconds)

    logger.info("Searching for feeds...")

    // handle any uncaught exceptions
    process.onUncaughtException { err =>
      logger.error("An uncaught exception was fired:", err.stack)
    }
  }

}
