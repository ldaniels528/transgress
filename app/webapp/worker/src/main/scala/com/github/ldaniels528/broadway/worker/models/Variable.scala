package com.github.ldaniels528.broadway.worker.models

import io.scalajs.util.JsUnderOrHelper._
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.Try

/**
  * Represents a variable
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Variable(val name: String) extends js.Object

/**
  * Variable Companion
  * @author lawrence.daniels@gmail.com
  */
object Variable {

  @js.native
  trait Unsafe extends js.Object {
    val name: js.UndefOr[String] = js.native
  }

  /**
    * VariableUnsafe Enrichment
    * @param variable the given [[Variable.Unsafe variable]]
    */
  final implicit class VariableUnsafeEnrichment(val variable: Variable.Unsafe) extends AnyVal {

    @inline
    def validate: Try[Variable] = {
      Try {
        new Variable(name = variable.name.orDie("Variable name is required"))
      }
    }
  }
  
}