package com.github.ldaniels528.bourne.worker.devices

import com.github.ldaniels528.bourne.rest.LoggerFactory
import com.github.ldaniels528.bourne.worker.models.Source
import com.github.ldaniels528.bourne.worker.util.LoaderUtilities._
import com.github.ldaniels528.bourne.worker.{Statistics, StatisticsGenerator}
import io.scalajs.JSON
import io.scalajs.nodejs.Error
import io.scalajs.nodejs.buffer.Buffer
import io.scalajs.nodejs.fs.ReadStream
import io.scalajs.nodejs.os.OS
import io.scalajs.nodejs.readline.{Readline, ReadlineOptions}
import io.scalajs.npm.csvtojson
import io.scalajs.npm.csvtojson.ConverterOptions

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.language.{existentials, reflectiveCalls}
import scala.scalajs.js

/**
  * Text File Input Device
  * @param source the [[Source input source]]
  * @param stream the [[ReadStream input stream]]
  */
class TextFileInputDevice(val source: Source, val stream: ReadStream)(implicit ec: ExecutionContext)
  extends InputDevice {
  private val logger = LoggerFactory.getLogger(getClass)

  override def close(): Future[Unit] = stream.closeAsync.future

  override def pause(): Future[Boolean] = Future.successful(stream.pause().isPaused())

  override def resume(): Future[Boolean] = Future.successful(!stream.resume().isPaused())

  override def start(onData: js.Any => Any,
                     onError: Error => Any,
                     onFinish: js.Any => Any)(implicit statsGen: StatisticsGenerator): Future[Statistics] = {
    logger.info(s"Starting to read from '${source.name}' (${source.path})...")
    val promise = Promise[Statistics]()

    // setup the statistics generator - asynchronously set the file size
    source.getFileSize foreach { size =>
      logger.info(s"${source.name}: ${source.path} is ${size.bytes}")
      statsGen.sourceFileSize = size
    }

    // update the bytes read on each data event
    stream.on("data", (data: Any) => data match {
      case v: Buffer => statsGen.bytesRead += v.length
      case v: String => statsGen.bytesRead += v.length
      case _ =>
    })

    // handle the input based on its format
    source.format match {
      case "csv" => setupDelimitedTextProcessing(",", promise, onData, onError, onFinish)
      case "fixed" => setupLineProcessing(fromFixed, promise, onData, onError, onFinish)
      case "json" => setupLineProcessing(fromJSON, promise, onData, onError, onFinish)
      case "psv" => setupDelimitedTextProcessing("|", promise, onData, onError, onFinish)
      case "tsv" => setupDelimitedTextProcessing("\t", promise, onData, onError, onFinish)
      case format =>
        promise.failure(js.JavaScriptException(s"Unhandled input format '$format'"))
    }
    promise.future
  }

  override def stop(): Future[Boolean] = stream.closeAsync.future.map(_ => true)

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
    )).onError(error => onError(error))
      .on("record_parsed", (data: js.Any) => onData(data))
      .on("end_parsed", (data: js.Any) => {
        onFinish(data)
        statsGen.update(force = true) foreach (stats => promise.success(stats))
      })

    stream.pipe(converter)
    ()
  }

  /**
    * Setups event-driven text line processing
    * @param promise  the completion [[Promise promise]]
    * @param onData   the data event handler
    * @param onFinish the completion event handler
    */
  private def setupLineProcessing(converter: String => js.Any,
                                  promise: Promise[Statistics],
                                  onData: js.Any => Any,
                                  onError: Error => Any,
                                  onFinish: js.Any => Any)(implicit statsGen: StatisticsGenerator) = {
    Readline.createInterface(new ReadlineOptions(input = stream))
      .on("error", (error: Error) => onError(error))
      .on("line", (line: String) => try onData(converter(line)) catch {
        case e: Throwable => onError(new Error(e.getMessage))
      })
      .on("close", () => {
        onFinish(OS.EOL)
        statsGen.update(force = true) foreach (stats => promise.success(stats))
      })
  }

  private def fromFixed(line: String) = {
    val result = source.fields.foldLeft((0, js.Dictionary[String]())) { case ((pos, dict), field) =>
      dict(field.name) = line.substring(pos, pos + field.length)
      (pos + field.length, dict)
    }
    result._2
  }

  private def fromJSON(line: String) = JSON.parse(line)

}
