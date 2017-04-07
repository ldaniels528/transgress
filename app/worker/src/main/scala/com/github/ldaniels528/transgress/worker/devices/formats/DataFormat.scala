package com.github.ldaniels528.transgress.worker.devices.formats

import com.github.ldaniels528.transgress.worker.models.Statistics
import com.github.ldaniels528.transgress.worker.{JobEventHandler, StatisticsGenerator}
import io.scalajs.nodejs.stream.Readable

import scala.concurrent.Promise
import scala.scalajs.js

/**
  * Data Format
  * @author lawrence.daniels@gmail.com
  */
trait DataFormat {

  def format(data: js.Any): js.Array[String]

  /**
    * Setups event-driven text format processing
    * @param stream   the [[Readable Readable]]
    * @param handler  the [[JobEventHandler job event handler]]
    * @param statsGen the [[StatisticsGenerator statistics generator]]
    */
  def start(stream: Readable)(implicit handler: JobEventHandler, statsGen: StatisticsGenerator): Promise[Statistics]

  override def toString: String = getClass.getSimpleName

}
