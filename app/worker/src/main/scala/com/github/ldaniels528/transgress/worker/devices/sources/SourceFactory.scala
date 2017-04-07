package com.github.ldaniels528.transgress.worker.devices
package sources

import com.github.ldaniels528.transgress.models.SourceLike
import com.github.ldaniels528.transgress.worker.models.Field._
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js
import scala.util.{Failure, Success, Try}

/**
  * Source Factory
  * @author lawrence.daniels@gmail.com
  */
object SourceFactory {

  def getSource(source: SourceLike): Try[Source] = {
    Try {
      val name = source.name.orDie("No name specified")
      val path = source.path.orDie(s"$name: No path specified")
      val `type` = source.`type`.orDie(s"$name: No type specified")
      val format = source.format.orDie(s"$name.${`type`}: No format specified")
      val columnHeaders = source.columnHeaders.isTrue
      val fields = source.fields.map(_.map(_.validate match {
        case Success(v) => v
        case Failure(e) => throw e
      })).getOrElse(js.Array())

      `type` match {
        case "file" => FileSource(name, path, `type`, format, columnHeaders, fields)
        case "url" => URLSource(name, path, `type`, format, columnHeaders, fields)
        case "mongodb" =>
          val mongoConnect = source.mongoConnect.toOption
          val mongoCollection = source.mongoCollection.toOption
          MongoSource(name, path, `type`, format, columnHeaders, fields, mongoConnect, mongoCollection)
      }
    }
  }

}
