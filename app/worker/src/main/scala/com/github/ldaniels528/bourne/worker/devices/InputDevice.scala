package com.github.ldaniels528.bourne.worker.devices

import com.github.ldaniels528.bourne.worker.{JobEventHandler, Statistics, StatisticsGenerator}

import scala.concurrent.Future

/**
  * Represents a generic input device
  * @author lawrence.daniels@gmail.com
  */
trait InputDevice {

  /**
    * Closes the input device
    * @return a completion promise
    */
  def close(): Future[Unit]

  /**
    * Pauses the input device
    * @return a promise of the status
    */
  def pause(): Future[Boolean]

  /**
    * Resumes the input device
    * @return a promise of the status
    */
  def resume(): Future[Boolean]

  /**
    * Starts the processing
    * @param handler the given [[JobEventHandler job event handler]]
    * @return a promise of the final [[Statistics statistics]]
    */
  def start(handler: JobEventHandler)(implicit statsGen: StatisticsGenerator): Future[Statistics]

  /**
    * Stops the input device
    * @return a promise of the status
    */
  def stop(): Future[Boolean]

}
