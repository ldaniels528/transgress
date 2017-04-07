package com.github.ldaniels528.transgress.worker.devices.output

import com.github.ldaniels528.transgress.worker.JobEventHandler
import com.github.ldaniels528.transgress.worker.devices.Device

import scala.concurrent.Future
import scala.scalajs.js

/**
  * Represents a generic output device
  * @author lawrence.daniels@gmail.com
  */
trait OutputDevice extends Device {

  /**
    * Flushes the device
    * @return the completion promise
    */
  def flush()(implicit jobEventHandler: JobEventHandler): Future[Int]

  /**
    * Writes a single record (or line of text) to the underlying persistence layer
    * @param data the given record, message or line of text
    * @param jobEventHandler the given [[JobEventHandler event handler]]
    */
  def write(data: js.Any)(implicit jobEventHandler: JobEventHandler): Unit

}
