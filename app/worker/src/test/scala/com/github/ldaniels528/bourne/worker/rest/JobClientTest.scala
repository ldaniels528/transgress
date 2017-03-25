package com.github.ldaniels528.bourne.worker.rest

import com.github.ldaniels528.bourne.models.JobStates
import com.github.ldaniels528.bourne.rest.Job
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
        name = "UserAgentsTSV",
        input = "./example/incoming/useragents_20170310.tsv",
        workflowConfig = "UserAgentsJSON",
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
