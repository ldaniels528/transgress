package com.github.ldaniels528.bourne

import io.scalajs.nodejs.Process

/**
  * Environment Helper
  * @author lawrence.daniels@gmail.com
  */
object EnvironmentHelper {
  val BOURNE_HOME = "BOURNE_HOME"
  val BOURNE_MASTER = "BOURNE_MASTER"
  val BOURNE_MONGODB = "BOURNE_MONGODB"

  /**
    * Process configuration extensions
    * @param process the given [[Process process]]
    */
  implicit class ProcessConfigExtensions(val process: Process) extends AnyVal {

    @inline
    def homeDirectory: Option[String] = process.env.get(BOURNE_HOME)

    @inline
    def master: Option[String] = process.env.get(BOURNE_MASTER)

    /**
      * Attempts to returns the web application listen port
      * @return the option of the web application listen port
      */
    @inline
    def port: Option[String] = process.env.find(_._1.equalsIgnoreCase("port")).map(_._2)

    /**
      * Attempts to returns the database connection URL
      * @return the option of the database connection URL
      */
    @inline
    def dbConnect: Option[String] = process.env.find(_._1.equalsIgnoreCase(BOURNE_MONGODB)).map(_._2)

    @inline
    def dbConnectOrDefault: String = dbConnect getOrElse "mongodb://localhost:27017/bourne"

  }

}
