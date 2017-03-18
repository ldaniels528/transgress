package com.github.ldaniels528.broadway.worker.devices

import io.scalajs.JSON
import io.scalajs.nodejs.fs.WriteStream
import io.scalajs.nodejs.os.OS

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * Represents a JSON output device
  * @param out the given [[WriteStream write stream]]
  */
class JSONOutputDevice(out: WriteStream)(implicit ec: ExecutionContext) extends OutputDevice {

  override def close(): Future[Unit] = out.closeAsync.future

  override def flush(): Future[Int] = Future.successful(0)

  override def write(data: js.Any): Future[Int] = {
    out.writeAsync(JSON.stringify(data) + OS.EOL).future.map(_ => 1)
  }

}