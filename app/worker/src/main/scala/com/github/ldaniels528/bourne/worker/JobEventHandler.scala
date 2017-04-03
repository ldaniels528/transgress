package com.github.ldaniels528.bourne.worker

import io.scalajs.nodejs.Error

import scala.concurrent.Future
import scala.scalajs.js

/**
  * Job Event Handler
  * @author lawrence.daniels@gmail.com
  */
trait JobEventHandler {

  /**
    * Called upon receipt of data
    * @param data the given data
    */
  def onData(data: js.Any): Unit

  /**
    * Called when an error occurs
    * @param err the given [[Error error]]
    */
  def onError(err: Error): Unit

  /**
    * Called once all data has been consumed
    * @param data the given data (usually an EOL character)
    * @return a completion promise
    */
  def onFinish(data: js.Any): Future[Unit]

}
