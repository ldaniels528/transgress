package com.github.ldaniels528.broadway.worker.devices

import io.scalajs.nodejs.fs.WriteStream
import io.scalajs.nodejs.os.OS

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

  override def flush(): Future[Int] = Future.successful(0)

  override def write(data: js.Any): Future[Int] = writeAsCSV(data)

  private def writeAsCSV(data: js.Any) = {
    val dict = data.asInstanceOf[js.Dictionary[js.Any]]
    if (columnHeaders && headers.isEmpty) {
      headers = dict.keySet.toSeq
      for {
        _ <- persistLine(headers.map(s => '"' + s + '"').mkString(delimiter))
        _ <- persistLine(toValuesString(dict))
      } yield 1
    }
    else {
      persistLine(toValuesString(dict))
    }
  }

  private def persistLine(line: String) = {
    out.writeAsync(line + OS.EOL).future.map(_ => 1)
  }

  private def toValuesString(dict: js.Dictionary[js.Any]) = {
    headers.map(dict.get(_).map(_.toString).getOrElse("")).map(s => '"' + s + '"').mkString(delimiter)
  }

}
