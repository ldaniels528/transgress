package com.github.ldaniels528.transgress.worker.models

import com.github.ldaniels528.transgress.models.VariableLike
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.Try

/**
  * Represents a variable
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Variable(val name: String,
               var value: js.UndefOr[js.Any] = js.undefined) extends js.Object

/**
  * Variable Companion
  * @author lawrence.daniels@gmail.com
  */
object Variable {

  /**
    * VariableLike Enrichment
    * @param variable the given [[VariableLike variable]]
    */
  final implicit class VariableUnsafeEnrichment(val variable: VariableLike) extends AnyVal {

    @inline
    def validate: Try[Variable] = {
      Try {
        new Variable(name = variable.name.orDie("Variable name is required"))
      }
    }
  }
  
}