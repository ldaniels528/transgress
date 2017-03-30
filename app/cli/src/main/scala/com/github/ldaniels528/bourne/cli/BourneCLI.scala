package com.github.ldaniels528.bourne
package cli

import scala.scalajs.js.JSConverters._
import com.github.ldaniels528.bourne.AppConstants._
import com.github.ldaniels528.bourne.rest.JobClient
import io.scalajs.JSON
import io.scalajs.nodejs.console
import io.scalajs.nodejs.fs.Fs
import io.scalajs.nodejs.repl._
import io.scalajs.util.ScalaJsHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success}

/**
  * JSON.Born Command Line Interface
  * @author lawrence.daniels@gmail.com
  */
object BourneCLI extends js.JSApp {

  @JSExport
  override def main(): Unit = run()

  def run(): Unit = {
    println(f"Starting the JSON.Born CLI v$Version%.1f...")

    implicit val jobClient = new JobClient("localhost:9000")
    val replServer = REPL.start(new REPLOptions(
      prompt = "json.born> "
      //,eval = myEval
    ))
    replServer.context.job = job
    replServer.context.jobs = jobs
  }

  def job(implicit jobClient: JobClient): js.Function = (aJobId: js.UndefOr[String]) => aJobId foreach { jobId =>
    jobClient.getJobByID(jobId) onComplete {
      case Success(Some(job)) => console.log(JSON.stringify(job, null, 4))
      case Success(None) =>
      case Failure(e) => console.error(e.getMessage)
    }
    ""
  }

  def jobs(implicit jobClient: JobClient): js.Function = () => {
    jobClient.getJobs onComplete {
      case Success(jobs) => console.log(JSON.stringify(jobs, null, 4))
      case Failure(e) => console.error(e.getMessage)
    }
    ""
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
