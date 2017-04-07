package com.github.ldaniels528.transgress.worker.devices.input

import com.github.ldaniels528.transgress.LoggerFactory
import com.github.ldaniels528.transgress.worker.devices.formats.DataFormatFactory
import com.github.ldaniels528.transgress.worker.devices.sources.Source
import com.github.ldaniels528.transgress.worker.models.Statistics
import com.github.ldaniels528.transgress.worker.util.LoaderUtilities._
import com.github.ldaniels528.transgress.worker.{JobEventHandler, StatisticsGenerator}
import io.scalajs.nodejs.buffer.Buffer
import io.scalajs.nodejs.fs.Fs
import io.scalajs.nodejs.stream.Readable
import io.scalajs.npm.gzipuncompressedsize.{GzipUncompressedSize => GUS}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.existentials
import scala.scalajs.js

/**
  * Streaming Input Device
  * @author lawrence.daniels@gmail.com
  */
abstract class StreamingInputDevice() extends InputDevice {
  protected val logger: LoggerFactory.Logger = LoggerFactory.getLogger(getClass)

  def source: Source

  def stream: Readable

  override def pause(): Future[Boolean] = Future.successful(stream.pause().isPaused())

  override def resume(): Future[Boolean] = Future.successful(!stream.resume().isPaused())

  override def start(handler: JobEventHandler)(implicit ec: ExecutionContext, statsGen: StatisticsGenerator): Future[Statistics] = {
    logger.info(s"Starting to read from '${source.name}' (${source.path})...")

    // setup the statistics generator - asynchronously set the file size
    getFileSize foreach { size =>
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
    DataFormatFactory.getFormat(source.format) match {
      case Some(format) => format.start(stream)(handler, statsGen).future
      case None => Future.failed(js.JavaScriptException(s"Unhandled data format '${source.format}' for source '${source.name}'"))
    }
  }

  private def getFileSize(implicit ec: ExecutionContext): Future[Double] = {
    if (isGzipped(source.path))
      GUS.fromFileAsync(source.path).future
    else
      Fs.statAsync(source.path).future.map(_.size)
  }

  def isGzipped(path: String): Boolean = path.toLowerCase.endsWith(".gz")

}
