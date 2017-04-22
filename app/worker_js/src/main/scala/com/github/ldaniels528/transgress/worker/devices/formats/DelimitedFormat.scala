package com.github.ldaniels528.transgress.worker.devices.formats

import com.github.ldaniels528.transgress.worker.models.Statistics
import com.github.ldaniels528.transgress.worker.{JobEventHandler, StatisticsGenerator}
import io.scalajs.nodejs.stream.Readable
import io.scalajs.npm.csvtojson.{Converter, ConverterOptions}

import scala.language.existentials
import scala.concurrent.{Future, Promise}
import scala.scalajs.js

/**
  * Delimited Format
  * @param delimiter the field delimiter (e.g. "|")
  * @author lawrence.daniels@gmail.com
  */
class DelimitedFormat(delimiter: String, columnHeaders: Boolean = true) extends DataFormat {
  private var headers = Seq[String]()

  override def format(data: js.Any): Seq[String] = {
    val lines = js.Array[String]()
    val dict = data.asInstanceOf[js.Dictionary[js.Any]]
    if (columnHeaders && headers.isEmpty) {
      headers = dict.keySet.toSeq
      lines.push(headers.map(s => '"' + s + '"').mkString(delimiter))
    }
    lines.push(headers.map(dict.get(_).map(_.toString).getOrElse("")).map(s => '"' + s + '"').mkString(delimiter))
    lines
  }

  /**
    * Setups event-driven delimited text format processing
    * @param handler the job event handler
    */
  def start(stream: Readable)(implicit handler: JobEventHandler, statsGen: StatisticsGenerator): Future[Statistics] = {
    val promise = Promise[Statistics]()
    val converter = new Converter(new ConverterOptions(
      constructResult = false,
      delimiter = delimiter
    )).onError(error => handler.onError(error))
      .onRecordParsed((data: js.Any) => handler.onData(data))
      .onEndParsed((data: js.Any) => {
        handler.onFinish(data)
        promise.success(statsGen.update())
      })

    stream.pipe(converter)
    promise.future
  }

}