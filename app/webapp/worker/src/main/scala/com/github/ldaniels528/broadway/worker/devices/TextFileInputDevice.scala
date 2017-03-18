package com.github.ldaniels528.broadway.worker.devices

import com.github.ldaniels528.broadway.rest.LoggerFactory
import com.github.ldaniels528.broadway.worker.models.Source
import com.github.ldaniels528.broadway.worker.util.LoaderUtilities._
import com.github.ldaniels528.broadway.worker.{Statistics, StatisticsGenerator}
import io.scalajs.nodejs.Error
import io.scalajs.nodejs.buffer.Buffer
import io.scalajs.nodejs.fs.ReadStream
import io.scalajs.npm.csvtojson
import io.scalajs.npm.csvtojson.ConverterOptions

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.scalajs.js

/**
  * Text File Input Device
  * @param source the [[Source input source]]
  * @param stream the [[ReadStream input stream]]
  */
class TextFileInputDevice(val source: Source, stream: ReadStream)(implicit ec: ExecutionContext)
  extends InputDevice {
  private val logger = LoggerFactory.getLogger(getClass)

  override def close(): Future[Unit] = stream.closeAsync.future

  override def start(onData: js.Any => Any,
                     onError: Error => Any,
                     onFinish: js.Any => Any)(implicit statsGen: StatisticsGenerator): Future[Statistics] = {
    logger.info(s"Starting to read from '${source.name}' (${source.path})...")
    val promise = Promise[Statistics]()

    // setup the statistics generator - asynchronously set the file size
    source.getFileSize foreach { size =>
      logger.info(s"${source.path} is ${size.bytes}")
      statsGen.sourceFileSize = size
    }

    // update the bytes read on each data event
    stream.onData[Buffer](statsGen.bytesRead += _.length)

    // handle the input based on its format
    source.format match {
      case "csv" => setupDelimitedTextProcessing(",", promise, onData, onError, onFinish)
      case "psv" => setupDelimitedTextProcessing("|", promise, onData, onError, onFinish)
      case "tsv" => setupDelimitedTextProcessing("\t", promise, onData, onError, onFinish)
      case format =>
        promise.failure(js.JavaScriptException(s"Unhandled input format '$format'"))
    }
    promise.future
  }

  /**
    * Setups event-driven delimited text format processing
    * @param delimiter the field delimiter (e.g. "|")
    * @param promise   the completion [[Promise promise]]
    * @param onData    the data event handler
    * @param onFinish  the completion event handler
    */
  private def setupDelimitedTextProcessing(delimiter: String,
                                           promise: Promise[Statistics],
                                           onData: js.Any => Any,
                                           onError: Error => Any,
                                           onFinish: js.Any => Any)(implicit statsGen: StatisticsGenerator) = {
    val converter = new csvtojson.Converter(new ConverterOptions(
      constructResult = false,
      delimiter = delimiter
    )).on("error", (err: Error) => onError(err))
      .on("record_parsed", (data: js.Any) => {
        statsGen.totalRead += 1
        onData(data)
      })
      .on("end_parsed", (data: js.Any) => {
        onFinish(data)
        statsGen.update(force = true) foreach (stats => promise.success(stats))
      })

    stream.pipe(converter)
    ()
  }

}
