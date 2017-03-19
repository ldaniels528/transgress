package com.github.ldaniels528.broadway.worker.models

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

  @js.native
  trait Unsafe extends js.Object {
    var name: js.UndefOr[String] = js.native
    var length: js.UndefOr[Int] = js.native
  }

  /**
    * FieldUnsafe Enrichment
    * @param field the given [[Field.Unsafe field]]
    */
  final implicit class FieldUnsafeEnrichment(val field: Field.Unsafe) extends AnyVal {

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