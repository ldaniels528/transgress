package com.github.ldaniels528.transgress.worker.devices.formats

import com.github.ldaniels528.transgress.worker.models.Statistics
import com.github.ldaniels528.transgress.worker.{JobEventHandler, StatisticsGenerator}
import io.scalajs.JSON
import io.scalajs.nodejs.Error
import io.scalajs.nodejs.os.OS
import io.scalajs.nodejs.readline.{Readline, ReadlineOptions}
import io.scalajs.nodejs.stream.Readable

import scala.concurrent.Promise
import scala.scalajs.js

/**
  * JSON Format
  * @author lawrence.daniels@gmail.com
  */
class JSONFormat(prettify: Boolean = false) extends DataFormat {

  override def format(data: js.Any): Seq[String] = {
    Seq(if (prettify) JSON.stringify(data, null, 4) else JSON.stringify(data))
  }

  /**
    * Setups event-driven text format processing
    * @param handler the job event handler
    */
  def start(stream: Readable)(implicit handler: JobEventHandler, statsGen: StatisticsGenerator): Promise[Statistics] = {
    val promise = Promise[Statistics]()
    Readline.createInterface(new ReadlineOptions(input = stream))
      .on("error", (error: Error) => handler.onError(error))
      .on("line", (line: String) => try handler.onData(JSON.parse(line)) catch {
        case e: Throwable => handler.onError(new Error(e.getMessage))
      })
      .on("close", () => {
        handler.onFinish(OS.EOL)
        promise.success(statsGen.update())
      })
    promise
  }

}
