package com.github.ldaniels528.broadway.worker

import io.scalajs.nodejs.path.Path
import io.scalajs.nodejs.process
import io.scalajs.util.OptionHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Worker Configuration
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class WorkerConfig(val baseDirectory: String) extends js.Object

/**
  * Worker Configuration Companion
  * @author lawrence.daniels@gmail.com
  */
object WorkerConfig {

  def apply(baseDirectory: js.UndefOr[String] = js.undefined): WorkerConfig = {
    new WorkerConfig(baseDirectory = baseDirectory.toOption ?? process.env.get("BROADWAY_HOME") getOrElse ".")
  }

  /**
    * Worker Config Methods
    * @param config the given [[WorkerConfig worker configuration]]
    */
  final implicit class WorkerConfigMethods(val config: WorkerConfig) extends AnyVal {

    @inline
    def archiveDirectory = s"${config.baseDirectory}/archive"

    @inline
    def archiveFile(file: String): js.UndefOr[String] = {
      val path = Path.parse(file)
      for {
        name <- path.name
        ext <- path.ext
      } yield s"$workDirectory/$name$ext"
    }

    @inline
    def incomingDirectory = s"${config.baseDirectory}/incoming"

    @inline
    def workDirectory = s"${config.baseDirectory}/work"

    @inline
    def workFile(file: String): js.UndefOr[String] = {
      val path = Path.parse(file)
      for {
        name <- path.name
        ext <- path.ext
      } yield s"$workDirectory/$name$ext"
    }

  }

}