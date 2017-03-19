package com.github.ldaniels528.broadway.worker

import scala.scalajs.js

/**
  * Workflow Configuration
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait WorkflowConfig extends js.Object {
  val name: js.UndefOr[String] = js.native
  val config: js.UndefOr[String] = js.native
  val patterns: js.UndefOr[js.Array[String]] = js.native
}

