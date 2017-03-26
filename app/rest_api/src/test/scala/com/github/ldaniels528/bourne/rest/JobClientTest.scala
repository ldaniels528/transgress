package com.github.ldaniels528.bourne.rest

import com.github.ldaniels528.bourne.models.JobStates
import io.scalajs.JSON
import org.scalatest.FunSpec

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Job Client Tests
  * @author lawrence.daniels@gmail.com
  */
class JobClientTest extends FunSpec {

  describe("JobClient") {

    it("can create jobs") {
      val jobClient = new JobClient("localhost:9000")
      val outcome = jobClient.createJob(new Job(
        _id = js.undefined,
        name = "useragents_20170310.tsv",
        input = "./example/incoming/useragents_20170310.tsv",
        inputSize = 1024.0,
        workflowName = "UserAgentsJSON",
        state = JobStates.NEW
      ))

      outcome onComplete {
        case Success(job) => info(s"job = ${JSON.stringify(job)}")
        case Failure(e) =>
          alert(e.getMessage)
      }
    }

  }

}
