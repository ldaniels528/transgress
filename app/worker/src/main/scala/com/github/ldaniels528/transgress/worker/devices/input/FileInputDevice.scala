package com.github.ldaniels528.transgress.worker.devices.input

import com.github.ldaniels528.transgress.worker.devices.sources.FileSource
import io.scalajs.nodejs.fs.{Fs, ReadStream}
import io.scalajs.nodejs.zlib.Zlib

import scala.concurrent.{ExecutionContext, Future}
import scala.language.{existentials, reflectiveCalls}

/**
  * File Input Device
  * @param source the [[FileSource input source]]
  */
class FileInputDevice(val source: FileSource)(implicit ec: ExecutionContext) extends StreamingInputDevice {

  val stream: ReadStream = {
    val stream = Fs.createReadStream(source.path)
    if (isGzipped(source.path)) stream.pipe(Zlib.createGunzip()) else stream
  }

  override def close(): Future[Unit] = stream.closeAsync.future

  override def stop(): Future[Boolean] = stream.closeAsync.future.map(_ => true)

}
