package com.github.ldaniels528.bourne.worker.devices

import com.github.ldaniels528.bourne.worker.{Statistics, StatisticsGenerator}
import io.scalajs.nodejs.Error

import scala.concurrent.Future
import scala.scalajs.js

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
    * @param onData the callback that is executed upon receipt of data
    * @param onError the callback that is executed when an error occurs
    * @param onFinish the callback that is executed when all data has been consumed
    * @param statsGen the [[StatisticsGenerator statistics generator]]
    * @return a promise of the final [[Statistics statistics]]
    */
  def start(onData: js.Any => Any,
            onError: Error => Any,
            onFinish: js.Any => Any)(implicit statsGen: StatisticsGenerator): Future[Statistics]

  /**
    * Stops the input device
    * @return a promise of the status
    */
  def stop(): Future[Boolean]

}
