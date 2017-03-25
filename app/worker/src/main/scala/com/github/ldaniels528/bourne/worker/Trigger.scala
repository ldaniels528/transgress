package com.github.ldaniels528.bourne.worker

import scala.scalajs.js

/**
  * Trigger Configuration
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait Trigger extends js.Object {
  val name: js.UndefOr[String] = js.native
  val priority: js.UndefOr[Int] = js.native
  val patterns: js.UndefOr[js.Array[String]] = js.native
  val workflowName: js.UndefOr[String] = js.native
}
