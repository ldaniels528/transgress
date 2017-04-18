package com.github.ldaniels528.transgress.worker.models

import com.github.ldaniels528.transgress.models.FieldLike
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.Try

/**
  * Represents a data field
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Field(val name: String, val length: Int) extends js.Object

/**
  * Field Companion
  * @author lawrence.daniels@gmail.com
  */
object Field {

  /**
    * FieldLike Enrichment
    * @param field the given [[FieldLike field]]
    */
  final implicit class FieldLikeEnrichment(val field: FieldLike) extends AnyVal {

    @inline
    def validate: Try[Field] = {
      Try {
        val name = field.name.orDie("No name specified")
        val length = field.length.orDie(s"$name: No length specified")
        new Field(name, length)
      }
    }
  }

}