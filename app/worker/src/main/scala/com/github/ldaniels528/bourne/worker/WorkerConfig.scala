package com.github.ldaniels528.bourne.worker

import com.github.ldaniels528.bourne.rest.LoggerFactory
import io.scalajs.JSON
import io.scalajs.nodejs.fs.Fs
import io.scalajs.nodejs.path.Path
import io.scalajs.nodejs.process
import io.scalajs.npm.moment.Moment
import io.scalajs.util.OptionHelper._

import scala.scalajs.js

/**
  * Worker Configuration
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait WorkerConfig extends js.Object {
  val baseDirectory: js.UndefOr[String] = js.native
  val master: js.UndefOr[String] = js.native
  val maxConcurrency: js.UndefOr[Int] = js.native
  val triggers: js.UndefOr[js.Array[Trigger]] = js.native
}

/**
  * Worker Configuration Companion
  * @author lawrence.daniels@gmail.com
  */
object WorkerConfig {
  private val logger = LoggerFactory.getLogger(getClass)

  def load(path: js.UndefOr[String] = js.undefined): WorkerConfig = {
    val configDirectory = path.toOption ?? process.env.get("BOURNE_HOME") getOrElse "."
    val configFile = s"$configDirectory/worker-config.json"
    logger.info(s"Loading '$configFile'...")
    JSON.parseAs[WorkerConfig](Fs.readFileSync(configFile).toString())
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
    def workDirectory = s"${config.baseDirectory}/work"

    @inline
    def workFile(file: String): js.UndefOr[String] = getPath(file, workDirectory)

    @inline
    def workflowDirectory = s"${config.baseDirectory}/workflows"

    @inline
    def workflow(file: String): js.UndefOr[String] = getPath(s"$file.json", workflowDirectory)

    private def getArchivePath(file: String, directory: String) = {
      val path = Path.parse(file)
      val yyyymmdd = Moment().format("YYYYMMDD")
      val hhmmss = Moment().format("HHmmss")
      for {
        name <- path.name
        ext = path.ext getOrElse ""
      } yield s"$directory/$yyyymmdd/$hhmmss/$name$ext"
    }

    private def getPath(file: String, directory: String) = {
      val path = Path.parse(file)
      for {
        name <- path.name
        ext = path.ext getOrElse ""
      } yield s"$directory/$name$ext"
    }

  }

}