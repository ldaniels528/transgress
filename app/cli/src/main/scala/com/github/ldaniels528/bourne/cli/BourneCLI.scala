package com.github.ldaniels528.bourne
package cli

import io.scalajs.util.ScalaJsHelper._
import com.github.ldaniels528.bourne.AppConstants._
import com.github.ldaniels528.bourne.rest.JobClient
import io.scalajs.nodejs.fs.Fs
import io.scalajs.nodejs.repl.REPLServer
import io.scalajs.nodejs.setTimeout
import io.scalajs.npm.otaatrepl.{OTaaTRepl, OTaaTReplOptions}
import io.scalajs.util.DurationHelper._

import scala.concurrent.Promise
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExport

/**
  * Transgress Command Line Interface
  * @author lawrence.daniels@gmail.com
  */
object BourneCLI extends js.JSApp {

  @JSExport
  override def main(): Unit = run()

  def run(): Unit = {
    println(f"Starting the JSON.Born CLI v$Version%.1f...")

    implicit val jobClient = new JobClient("localhost:9000")
    val replServer = OTaaTRepl.start(new OTaaTReplOptions(
      prompt = "json.born> "
      //,eval = myEval
    ))
    replServer.context.job = job
    replServer.context.jobs = jobs
    replServer.context.workers = workers
  }

  def job(implicit jobClient: JobClient): js.Function = (aJobId: js.UndefOr[String]) => aJobId map { jobId =>
    jobClient.getJobByID(jobId).map(_.orNull).toJSPromise
  }

  def jobs(implicit jobClient: JobClient): js.Function = () => {
    jobClient.getJobs.toJSPromise
  }

  def workers(implicit jobClient: JobClient): js.Function = () => {
    val promise = Promise[String]()
    setTimeout(() => promise.success("Hello"), 1.second)
    promise.future.toJSPromise
  }

  def myEval: js.Function = (cmd: String, context: js.Dynamic, filename: String, callback: js.Function2[Error, js.Any, Any]) => {
    val result = cmd.trim
    callback(null, result)
  }

  def loadHistory(server: REPLServer): Array[Any] = {
    Fs.readFileSync(".node_repl_history", encoding = "utf8")
      .split("\n")
      .reverse
      .map(line => line.trim())
      .map(line => server.dynamic.history.push(line))
  }

}
