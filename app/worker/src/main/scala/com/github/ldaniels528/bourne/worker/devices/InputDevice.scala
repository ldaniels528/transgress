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

  def close(): Future[Unit]

  def start(onData: js.Any => Any,
            onError: Error => Any,
            onFinish: js.Any => Any)(implicit statsGen: StatisticsGenerator): Future[Statistics]

}
