package com.github.ldaniels528.broadway.worker.devices

import com.github.ldaniels528.broadway.rest.LoggerFactory
import com.github.ldaniels528.broadway.worker.ExpressionEvaluator
import com.github.ldaniels528.broadway.worker.models.Source
import io.scalajs.nodejs.fs.Fs

import scala.concurrent.ExecutionContext

/**
  * Device Factory
  * @author lawrence.daniels@gmail.com
  */
object DeviceFactory {
  private val logger = LoggerFactory.getLogger(getClass)

  def getInputDevice(source: Source)(implicit ec: ExecutionContext): Option[InputDevice] = {
    source.`type` match {
      case "file" => Some(new TextFileInputDevice(source, source.createReadStream))
      case kind =>
        logger.error(s"Unhandled input source type '$kind'")
        None
    }
  }

  def getOutputDevice(source: Source)(implicit ec: ExecutionContext): Option[OutputDevice] = {
    source.`type` match {
      case "file" =>
        val stream = Fs.createWriteStream(source.path)
        source.format match {
          case "csv" => Some(new DelimitedOutputDevice(stream, delimiter = ","))
          case "fixed" => Some(new FixedOutputDevice(source, stream))
          case "json" => Some(new JSONOutputDevice(stream))
          case "psv" => Some(new DelimitedOutputDevice(stream, delimiter = "|"))
          case "tsv" => Some(new DelimitedOutputDevice(stream, delimiter = "\t"))
          case format =>
            logger.error(s"Unhandled file output source format '$format'")
            None
        }
      case "mongo" =>
        for {
          mongoConnect <- source.mongoConnect
          collection <- source.mongoCollection
        } yield new MongoDBOutputDevice(mongoConnect, collection)
      case kind =>
        logger.error(s"Unhandled output source type '$kind'")
        None
    }
  }

}
