package com.github.ldaniels528.transgress.worker.devices

import com.github.ldaniels528.transgress.worker.JobEventHandler
import io.scalajs.nodejs.fs.WriteStream
import io.scalajs.nodejs.os.OS
import io.scalajs.util.ScalaJsHelper.isDefined

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * Represents a delimited output device
  * @param out       the given [[WriteStream write stream]]
  * @param delimiter the given field delimiter (e.g. "|")
  */
class DelimitedOutputDevice(out: WriteStream, delimiter: String, columnHeaders: Boolean = true)(implicit ec: ExecutionContext)
  extends OutputDevice {
  private var headers = Seq[String]()

  override def close(): Future[Unit] = out.closeAsync.future

  override def flush()(implicit jobEventHandler: JobEventHandler): Future[Int] = Future.successful(0)

  override def write(data: js.Any)(implicit jobEventHandler: JobEventHandler): Unit = {
    val dict = data.asInstanceOf[js.Dictionary[js.Any]]
    if (columnHeaders && headers.isEmpty) {
      headers = dict.keySet.toSeq
      persistLine(
        headers.map(s => '"' + s + '"').mkString(delimiter),
        toValuesString(dict))
    }
    else persistLine(toValuesString(dict))
  }

  private def persistLine(lines: String*)(implicit jobEventHandler: JobEventHandler) {
    out.write(lines.mkString(OS.EOL), error => {
      if (isDefined(error)) jobEventHandler.onError(error)
      else {
        // TODO write statistics?
      }
    })
  }

  private def toValuesString(dict: js.Dictionary[js.Any]) = {
    headers.map(dict.get(_).map(_.toString).getOrElse("")).map(s => '"' + s + '"').mkString(delimiter)
  }

}
