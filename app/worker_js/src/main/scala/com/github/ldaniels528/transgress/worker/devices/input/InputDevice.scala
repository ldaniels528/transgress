package com.github.ldaniels528.transgress.worker.devices.input

import com.github.ldaniels528.transgress.worker.devices.Device
import com.github.ldaniels528.transgress.worker.models.Statistics
import com.github.ldaniels528.transgress.worker.{JobEventHandler, StatisticsGenerator}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Represents a generic input device
  * @author lawrence.daniels@gmail.com
  */
trait InputDevice extends Device {

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
  def start(handler: JobEventHandler)(implicit ec: ExecutionContext, statsGen: StatisticsGenerator): Future[Statistics]

  /**
    * Stops the input device
    * @return a promise of the status
    */
  def stop(): Future[Boolean]

}
