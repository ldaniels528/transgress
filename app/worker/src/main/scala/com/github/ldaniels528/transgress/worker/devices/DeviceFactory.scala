package com.github.ldaniels528.transgress.worker.devices

import com.github.ldaniels528.transgress.worker.devices.input._
import com.github.ldaniels528.transgress.worker.devices.output._
import com.github.ldaniels528.transgress.worker.devices.sources._

import scala.concurrent.ExecutionContext

/**
  * Device Factory
  * @author lawrence.daniels@gmail.com
  */
object DeviceFactory {

  def getInputDevice(source: Source)(implicit ec: ExecutionContext): Option[StreamingInputDevice] = {
    source match {
      case f: FileSource => Option(new FileInputDevice(f))
      case u: URLSource => Option(new URLInputDevice(u))
      case _ => None
    }
  }

  def getOutputDevice(source: Source)(implicit ec: ExecutionContext): Option[OutputDevice] = {
    source match {
      case fSource: FileSource => Option(new FileOutputDevice(fSource))
      case mSource: MongoSource =>
        for {
          mongoConnect <- mSource.mongoConnect
          collection <- mSource.mongoCollection
        } yield new MongoDBOutputDevice(mongoConnect, collection)
      case _ => None
    }
  }

}
