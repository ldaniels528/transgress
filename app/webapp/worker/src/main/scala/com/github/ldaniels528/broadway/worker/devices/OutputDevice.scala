package com.github.ldaniels528.broadway.worker.devices

import scala.concurrent.Future
import scala.scalajs.js

/**
  * Represents a generic output device
  * @author lawrence.daniels@gmail.com
  */
trait OutputDevice {

  def close(): Future[Unit]

  def flush(): Future[Int]

  def write(data: js.Any): Future[Int]

}
