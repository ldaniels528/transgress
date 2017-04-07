package com.github.ldaniels528.transgress.worker.devices.formats

import com.github.ldaniels528.transgress.worker.devices.formats.FixedLengthFormat._
import com.github.ldaniels528.transgress.worker.models.{Field, Statistics}
import com.github.ldaniels528.transgress.worker.{JobEventHandler, StatisticsGenerator}
import io.scalajs.nodejs.Error
import io.scalajs.nodejs.os.OS
import io.scalajs.nodejs.readline.{Readline, ReadlineOptions}
import io.scalajs.nodejs.stream.Readable

import scala.concurrent.Promise
import scala.scalajs.js

/**
  * Fixed Length Format
  * @author lawrence.daniels@gmail.com
  */
class FixedLengthFormat(fields: Seq[Field]) extends DataFormat {

  override def format(data: js.Any): js.Array[String] = {
    val dict = data.asInstanceOf[js.Dictionary[js.Any]]
    js.Array(fields.map(field => dict.get(field.name).map(_.toString).getOrElse("").sizeTo(field.length)).mkString)
  }

  /**
    * Setups event-driven text format processing
    * @param handler the job event handler
    */
  def start(stream: Readable)(implicit handler: JobEventHandler, statsGen: StatisticsGenerator): Promise[Statistics] = {
    val promise = Promise[Statistics]()
    Readline.createInterface(new ReadlineOptions(input = stream))
      .on("error", (error: Error) => handler.onError(error))
      .on("line", (line: String) => try handler.onData(fromFixed(line)) catch {
        case e: Throwable => handler.onError(new Error(e.getMessage))
      })
      .on("close", () => {
        handler.onFinish(OS.EOL)
        promise.success(statsGen.update())
      })
    promise
  }

  private def fromFixed(line: String): js.Dictionary[String] = {
    val result = fields.foldLeft((0, js.Dictionary[String]())) { case ((pos, dict), field) =>
      dict(field.name) = line.substring(pos, pos + field.length)
      (pos + field.length, dict)
    }
    result._2
  }

}

/**
  * Fixed-length Format Companion
  * @author lawrence.daniels@gmail.com
  */
object FixedLengthFormat {

  /**
    * String Enrichment
    * @param string the given [[String]]
    */
  final implicit class StringEnrichment(val string: String) extends AnyVal {

    @inline
    def sizeTo(length: Int): String = {
      string match {
        case s if s.length > length => s.substring(0, length)
        case s if s.length < length => s + " " * (length - s.length)
        case s => s
      }
    }

  }

}
