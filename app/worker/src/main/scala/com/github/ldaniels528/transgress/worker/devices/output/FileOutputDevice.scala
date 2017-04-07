package com.github.ldaniels528.transgress.worker.devices.output

import com.github.ldaniels528.transgress.worker.JobEventHandler
import com.github.ldaniels528.transgress.worker.devices.formats.DataFormatFactory
import com.github.ldaniels528.transgress.worker.devices.sources.{FileSource, Source}
import io.scalajs.nodejs.fs.Fs
import io.scalajs.nodejs.os.OS
import io.scalajs.util.OptionHelper._
import io.scalajs.util.ScalaJsHelper.isDefined

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * File Output Device
  * @param source the given [[Source source]]
  */
class FileOutputDevice(source: FileSource)(implicit ec: ExecutionContext) extends OutputDevice {
  private val out = Fs.createWriteStream(source.path)
  private val format = DataFormatFactory.getFormat(source.format).
    orDie(s"Unhandled format '${source.format}' for source '${source.name}'")

  override def close(): Future[Unit] = out.closeAsync.future

  override def flush()(implicit jobEventHandler: JobEventHandler): Future[Int] = Future.successful(0)

  override def write(data: js.Any)(implicit jobEventHandler: JobEventHandler): Unit = {
    format.format(data) foreach { line =>
      out.write(line + OS.EOL, error => {
        if (isDefined(error)) jobEventHandler.onError(error)
        else {
          // TODO write statistics?
        }
      })
    }
  }

}
