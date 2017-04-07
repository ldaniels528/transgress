package com.github.ldaniels528.transgress.worker.devices.input

import com.github.ldaniels528.transgress.worker.devices.sources.URLSource
import io.scalajs.nodejs.stream.Readable
import io.scalajs.npm.request.Request

import scala.concurrent.{ExecutionContext, Future}

/**
  * URL Input Device
  * @param source the [[URLSource input source]]
  */
class URLInputDevice(val source: URLSource)(implicit ec: ExecutionContext) extends StreamingInputDevice {

  val stream: Readable = Request.get(source.path)

  override def close(): Future[Unit] = Future.successful(()) //stream.closeAsync.future

  override def stop(): Future[Boolean] = Future.successful(true) //stream.closeAsync.future.map(_ => true)

}
