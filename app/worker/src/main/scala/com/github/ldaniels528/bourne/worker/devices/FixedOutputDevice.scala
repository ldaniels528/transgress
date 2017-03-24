package com.github.ldaniels528.bourne.worker.devices

import com.github.ldaniels528.bourne.worker.devices.FixedOutputDevice._
import com.github.ldaniels528.bourne.worker.models.Source
import io.scalajs.nodejs.fs.WriteStream
import io.scalajs.nodejs.os.OS

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.scalajs.js

/**
  * Fixed-length Output Device
  * @param out the given [[WriteStream write stream]]
  */
class FixedOutputDevice(source: Source, out: WriteStream)(implicit ec: ExecutionContext) extends OutputDevice {

  override def close(): Future[Unit] = out.closeAsync.future

  override def flush(): Future[Int] = Future.successful(0)

  override def write(data: js.Any): Future[Int] = {
    val dict = data.asInstanceOf[js.Dictionary[js.Any]]
    val line = source.fields.map(field => dict.get(field.name).map(_.toString).getOrElse("").sizeTo(field.length)).mkString
    out.writeAsync(line + OS.EOL).future.map(_ => 1)
  }

}

/**
  * Fixed-length Output Device Companion
  * @author lawrence.daniels@gmail.com
  */
object FixedOutputDevice {

  /**
    * String Enrichment
    * @param string the given [[String]]
    */
  final implicit class StringEnrichment(val string: String) extends AnyVal {

    @inline
    def sizeTo(length: Int): String = {
      string match {
        case s if s.length > length => s.substring(0, length)
        case s if s.length < length => s + " " * (length - s.length)
        case s => s
      }
    }

  }

}
