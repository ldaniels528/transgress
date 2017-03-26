package com.github.ldaniels528.bourne.worker

import com.github.ldaniels528.bourne.rest.LoggerFactory
import io.scalajs.JSON
import io.scalajs.nodejs.fs.Fs
import io.scalajs.nodejs.os.OS
import io.scalajs.nodejs.path.Path
import io.scalajs.npm.glob.Glob
import io.scalajs.npm.moment.Moment
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js
import scala.util.Try

/**
  * Worker Configuration
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait WorkerConfig extends js.Object {
  var baseDirectory: js.UndefOr[String] = js.native
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
    def archiveDirectory = s"${config.baseDirectory}/archive"

    @inline
    def archiveFile(file: String): js.UndefOr[String] = getArchivePath(file, archiveDirectory)

    @inline
    def getMaxConcurrency: Int = config.maxConcurrency getOrElse 1

    @inline
    def incomingDirectory = s"${config.baseDirectory}/incoming"

    @inline
    def triggerDirectory = s"${config.baseDirectory}/config/triggers"

    @inline
    def triggerFile(file: String): js.UndefOr[String] = getPath(s"$file.json", triggerDirectory)

    @inline
    def workDirectory = s"${config.baseDirectory}/work"

    @inline
    def workFile(file: String): js.UndefOr[String] = getPath(file, workDirectory)

    @inline
    def workflowDirectory = s"${config.baseDirectory}/config/workflows"

    @inline
    def workflowFile(file: String): js.UndefOr[String] = getPath(s"$file.json", workflowDirectory)

    /**
      * Returns the archive path for the given file
      * @param file      the given file
      * @param directory the given archive directory
      * @return the archive path (e.g. "./example/archive/20170327/181910/customers.csv")
      */
    private def getArchivePath(file: String, directory: String) = {
      val path = Path.parse(file)
      val yyyymmdd = Moment().format("YYYYMMDD")
      val hhmmss = Moment().format("HHmmss")
      for {
        name <- path.name
        ext = path.ext getOrElse ""
      } yield s"$directory/$yyyymmdd/$hhmmss/$name$ext".replaceAllLiterally(OS.EOL * 2, OS.EOL)
    }

    /**
      * Returns the sub-directory path for the given file
      * @param file      the given file
      * @param directory the given base directory
      * @return the path (e.g. "./example/workflows/customers_workflow.json")
      */
    private def getPath(file: String, directory: String) = {
      Path.parse(file).base.map(base => s"$directory/$base").map(_.replaceAllLiterally(OS.EOL * 2, OS.EOL))
    }

  }

}