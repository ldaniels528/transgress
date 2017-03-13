package com.github.ldaniels528.broadway.worker

import com.github.ldaniels528.broadway.rest.LoggerFactory
import io.scalajs.JSON
import io.scalajs.nodejs.fs.Fs

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * Represents a Workflow
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait Workflow extends js.Object {
  val input: js.UndefOr[String] = js.native
  val output: js.UndefOr[js.Array[String]] = js.native
  val onError: js.UndefOr[OnError] = js.native
  val sources: js.UndefOr[js.Array[Source]] = js.native
}

@js.native
trait Source extends js.Object {
  val name: js.UndefOr[String] = js.native
  val path: js.UndefOr[String] = js.native
  val `type`: js.UndefOr[String] = js.native
  val format: js.UndefOr[String] = js.native
  val columnHeaders: js.UndefOr[Boolean] = js.native
}

@js.native
trait OnError extends js.Object {
  val source: js.UndefOr[String] = js.native
}

/**
  * Workflow Companion
  * @author lawrence.daniels@gmail.com
  */
object Workflow {
  private val logger = LoggerFactory.getLogger(getClass)

  def load(path: String)(implicit ec: ExecutionContext): Future[Workflow] = {
    logger.info(s"Loading workflow '$path'...")
    Fs.readFileAsync(path).future map (buf => JSON.parseAs[Workflow](buf.toString()))
  }

}