package com.github.ldaniels528.transgress.worker.devices

import com.github.ldaniels528.transgress.worker.models.Source
import io.scalajs.nodejs.fs.Fs
import io.scalajs.util.OptionHelper._

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.util.Try

/**
  * Device Factory
  * @author lawrence.daniels@gmail.com
  */
object DeviceFactory {

  def getInputDevice(source: Source)(implicit ec: ExecutionContext): Try[InputDevice] = Try {
    source.`type` match {
      case "file" => new TextFileInputDevice(source, source.createReadStream)
      case kind =>
        throw js.JavaScriptException(s"Unhandled input source type '$kind'")
    }
  }

  def getOutputDevice(source: Source)(implicit ec: ExecutionContext): Try[OutputDevice] = Try {
    source.`type` match {
      case "file" =>
        val stream = Fs.createWriteStream(source.path)
        source.format match {
          case "csv" => new DelimitedOutputDevice(stream, delimiter = ",")
          case "fixed" => new FixedOutputDevice(source, stream)
          case "json" => new JSONOutputDevice(stream)
          case "psv" => new DelimitedOutputDevice(stream, delimiter = "|")
          case "tsv" => new DelimitedOutputDevice(stream, delimiter = "\t")
          case format =>
            throw js.JavaScriptException(s"Unhandled file output source format '$format'")
        }
      case "mongo" =>
        (for {
          mongoConnect <- source.mongoConnect
          collection <- source.mongoCollection
        } yield new MongoDBOutputDevice(mongoConnect, collection)) orDie "Invalid MongoDB configuration"
      case kind =>
        throw js.JavaScriptException(s"Unhandled output source type '$kind'")
    }
  }

}
