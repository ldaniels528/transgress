package com.github.ldaniels528.bourne
package cli

import com.github.ldaniels528.bourne.AppConstants._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

/**
  * Bourne Command Line Interface
  * @author lawrence.daniels@gmail.com
  */
object BourneCLI extends js.JSApp {

  @JSExport
  override def main(): Unit = run()

  def run(): Unit = {
    println(f"Starting the Bourne CLI v$Version%.1f...")
  }

}
