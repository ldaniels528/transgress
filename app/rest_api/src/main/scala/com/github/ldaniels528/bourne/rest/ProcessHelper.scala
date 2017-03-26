package com.github.ldaniels528.bourne.rest

import io.scalajs.nodejs.Process

/**
  * Bourne Process Helper
  * @author lawrence.daniels@gmail.com
  */
object ProcessHelper {

  /**
    * Process configuration extensions
    * @param process the given [[Process process]]
    */
  implicit class ProcessConfigExtensions(val process: Process) extends AnyVal {

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
    def dbConnect: Option[String] = process.env.find(_._1.equalsIgnoreCase("bourne_db")).map(_._2)

  }

}