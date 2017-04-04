package com.github.ldaniels528.transgress.worker.devices

import com.github.ldaniels528.transgress.worker.JobEventHandler
import io.scalajs.JSON
import io.scalajs.nodejs.fs.WriteStream
import io.scalajs.nodejs.os.OS
import io.scalajs.util.ScalaJsHelper.isDefined

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * Represents a JSON output device
  * @param out the given [[WriteStream write stream]]
  */
class JSONOutputDevice(out: WriteStream)(implicit ec: ExecutionContext) extends OutputDevice {

  override def close(): Future[Unit] = out.closeAsync.future

  override def flush()(implicit jobEventHandler: JobEventHandler): Future[Int] = Future.successful(0)

  override def write(data: js.Any)(implicit jobEventHandler: JobEventHandler): Unit = {
    out.write(JSON.stringify(data) + OS.EOL, error => {
      if (isDefined(error)) jobEventHandler.onError(error)
      else {
        // TODO write statistics?
      }
    })
  }

}