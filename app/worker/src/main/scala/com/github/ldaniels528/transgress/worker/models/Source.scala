package com.github.ldaniels528.transgress.worker
package models

import com.github.ldaniels528.transgress.models.SourceLike
import com.github.ldaniels528.transgress.worker.models.Field._
import io.scalajs.nodejs.fs.{Fs, ReadStream}
import io.scalajs.nodejs.zlib.Zlib
import io.scalajs.npm.gzipuncompressedsize.{GzipUncompressedSize => GUS}
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.{Failure, Success, Try}

/**
  * Represents a Workflow Source
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Source(val name: String,
             var path: String,
             val `type`: String,
             val format: String,
             val columnHeaders: Boolean,
             val fields: js.Array[Field],
             val mongoConnect: Option[String],
             val mongoCollection: Option[String]) extends js.Object

/**
  * Source Companion
  * @author lawrence.daniels@gmail.com
  */
object Source {

  /**
    * Source Enrichment
    * @param source the given [[Source source]]
    */
  final implicit class SourceEnrichment(val source: Source) extends AnyVal {

    @inline
    def createReadStream: ReadStream = {
      val stream = Fs.createReadStream(source.path)
      if (source.isGzipped) stream.pipe(Zlib.createGunzip()) else stream
    }

    @inline
    def getFileSize(implicit ec: ExecutionContext): Future[Double] = {
      if (source.isGzipped)
        GUS.fromFileAsync(source.path).future
      else
        Fs.statAsync(source.path).future.map(_.size)
    }

    @inline
    def isGzipped: Boolean = source.path.toLowerCase.endsWith(".gz")

  }

  /**
    * SourceLike Enrichment
    * @param source the given [[SourceLike source]]
    */
  final implicit class SourceLikeEnrichment(val source: SourceLike) extends AnyVal {

    @inline
    def validate: Try[Source] = {
      Try {
        val name = source.name.orDie("No name specified")
        val path = source.path.orDie(s"$name: No path specified")
        val `type` = source.`type`.orDie(s"$name: No type specified")
        val format = source.format.orDie(s"$name.${`type`}: No format specified")
        val columnHeaders = source.columnHeaders.isTrue
        val fields = source.fields.map(_.map(_.validate match {
          case Success(v) => v
          case Failure(e) => throw js.JavaScriptException(e.getMessage)
        })).getOrElse(js.Array())
        val mongoConnect = source.mongoConnect.toOption
        val mongoCollection = source.mongoCollection.toOption
        new Source(name, path, `type`, format, columnHeaders, fields, mongoConnect, mongoCollection)
      }
    }
  }

}


