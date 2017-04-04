package com.github.ldaniels528.transgress.watcher

import com.github.ldaniels528.transgress.watcher.models.Trigger
import com.github.ldaniels528.transgress.{LoggerFactory, TransgressConfig}
import io.scalajs.JSON
import io.scalajs.nodejs.fs.Fs
import io.scalajs.npm.glob.Glob
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js
import scala.util.Try

/**
  * Watcher Configuration
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait WatcherConfig extends TransgressConfig {
  var master: js.UndefOr[String] = js.native
  var triggers: js.UndefOr[js.Dictionary[Trigger]] = js.native
}

/**
  * Watcher Configuration Companion
  * @author lawrence.daniels@gmail.com
  */
object WatcherConfig {
  private val logger = LoggerFactory.getLogger(getClass)

  def load(configDirectory: String): WatcherConfig = {
    val configFile = s"$configDirectory/watcher-config.json"
    logger.info(s"Loading '$configFile'...")

    // load the watcher configuration file
    val config = JSON.parseAs[WatcherConfig](Fs.readFileSync(configFile).toString())

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

}