package com.github.ldaniels528.transgress.worker

import com.github.ldaniels528.transgress.worker.models.Trigger
import com.github.ldaniels528.transgress.{LoggerFactory, TransgressConfig}
import io.scalajs.JSON
import io.scalajs.nodejs.fs.Fs
import io.scalajs.npm.glob.Glob
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js
import scala.util.Try

/**
  * Worker Configuration
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait WorkerConfig extends TransgressConfig {
  var master: js.UndefOr[String] = js.native
  var maxConcurrency: js.UndefOr[Int] = js.native
  var triggers: js.UndefOr[js.Dictionary[Trigger]] = js.native
}

/**
  * Worker Configuration Companion
  * @author lawrence.daniels@gmail.com
  */
object WorkerConfig {
  private val logger = LoggerFactory.getLogger(getClass)

  def load(configDirectory: String): WorkerConfig = {
    val configFile = s"$configDirectory/worker-config.json"
    logger.info(s"Loading '$configFile'...")

    // load the worker configuration file
    val config = JSON.parseAs[WorkerConfig](Fs.readFileSync(configFile).toString())

    // set the base directory
    config.baseDirectory = configDirectory

    // make sure the config triggers dictionary exists
    config.triggers = config.triggers ?? js.Dictionary[Trigger]()

    // load the trigger configuration files
    Glob.sync(s"${config.triggerDirectory}/*.json").toSeq foreach { path =>
      Try {
        logger.info(s"Loading trigger file '$path'...")
        val trigger = JSON.parseAs[Trigger](Fs.readFileSync(path).toString())
        for {
          triggers <- config.triggers
          name <- trigger.name
        } triggers(name) = trigger
      }
    }

    config
  }

  /**
    * Worker Config Methods
    * @param config the given [[WorkerConfig worker configuration]]
    */
  final implicit class WorkerConfigMethods(val config: WorkerConfig) extends AnyVal {

    @inline
    def getMaxConcurrency: Int = config.maxConcurrency getOrElse 1

  }

}