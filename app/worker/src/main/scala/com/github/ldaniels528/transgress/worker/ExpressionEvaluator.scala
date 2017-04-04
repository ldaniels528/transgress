package com.github.ldaniels528.transgress.worker

import com.github.ldaniels528.transgress.LoggerFactory
import io.scalajs.nodejs.process
import io.scalajs.npm.moment.Moment

import scala.scalajs.js

/**
  * Expression Evaluator
  * @author lawrence.daniels@gmail.com
  */
object ExpressionEvaluator {
  private val logger = LoggerFactory.getLogger(getClass)
  private val startSeq = "{{"
  private val endSeq = "}}"
  private val variables = js.Dictionary(
    "$date" -> ((args: List[String]) => $date(args)),
    "$env" -> ((args: List[String]) => $env(args))
  )

  def evaluate(expression: String): String = {
    val sb = new StringBuilder(expression)
    var found = true
    do {
      val (start, end) = (sb.indexOf(startSeq), sb.indexOf(endSeq))
      found = start >= 0 && end > start
      if (found) {
        val expr = sb.substring(start + startSeq.length, end).trim
        // $date:YYYYMMDD
        val (function, args) = expr.split("[:]").toList match {
          case fx :: fxArgs => (fx, fxArgs)
          case _ => throw js.JavaScriptException(s"Invalid expression - '$expr'")
        }
        variables.get(function) foreach { fx =>
          sb.replace(start, end + endSeq.length, fx(args))
        }
      }
    } while (found)
    sb.toString()
  }

  def $date(args: List[String]): String = {
    args.headOption match {
      case Some(format) => Moment().format(format)
      case None => Moment().format()
    }
  }

  def $env(args: List[String]): String = {
    args.headOption match {
      case Some(key) => process.env.getOrElse(key, "")
      case None => ""
    }
  }

}
