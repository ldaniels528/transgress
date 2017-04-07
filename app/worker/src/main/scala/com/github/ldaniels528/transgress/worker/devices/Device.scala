package com.github.ldaniels528.transgress.worker.devices

import scala.concurrent.Future

/**
  * Represents a generic device
  * @author lawrence.daniels@gmail.com
  */
trait Device {

  /**
    * Closes the input device
    * @return a completion promise
    */
  def close(): Future[Unit]

}
