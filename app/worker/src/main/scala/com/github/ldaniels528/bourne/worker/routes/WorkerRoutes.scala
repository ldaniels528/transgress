package com.github.ldaniels528.bourne.worker.routes

import com.github.ldaniels528.bourne.models.JobStates
import com.github.ldaniels528.bourne.rest.JobClient
import com.github.ldaniels528.bourne.worker.JobFileTracking
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Job Routes
  * @author lawrence.daniels@gmail.com
  */
class WorkerRoutes(app: Application, jobTracker: JobFileTracking)(implicit jobClient: JobClient, ec: ExecutionContext) {

  ///////////////////////////////////////////////////////////////
  //    Routes
  ///////////////////////////////////////////////////////////////

  app.get("/api/worker/job/:id/pause", (request: Request, response: Response, next: NextFunction) => {
    val id = request.params.apply("id")
    val outcome = for {
      job_? <- jobClient.getJobByID(id)
      (job, success) <- job_? match {
        case Some(job) => jobTracker.pauseJob(job).map(job -> _)
        case None => Future.failed(js.JavaScriptException(s"job $id not found"))
      }
    } yield (job, success)

    outcome onComplete {
      case Success((job, success)) =>
        if (success) job.state = JobStates.PAUSED
        response.send(job)
        next()
      case Failure(e) => response.internalServerError(e); next()
    }
  })

  app.get("/api/worker/job/:id/resume", (request: Request, response: Response, next: NextFunction) => {
    val id = request.params.apply("id")
    val outcome = for {
      job_? <- jobClient.getJobByID(id)
      (job, success) <- job_? match {
        case Some(job) => jobTracker.resumeJob(job).map(job -> _)
        case None => Future.failed(js.JavaScriptException(s"job $id not found"))
      }
    } yield (job, success)

    outcome onComplete {
      case Success((job, success)) =>
        if (success) job.state = JobStates.RUNNING
        response.send(job)
        next()
      case Failure(e) => response.internalServerError(e); next()
    }
  })

  app.get("/api/worker/job/:id/stop", (request: Request, response: Response, next: NextFunction) => {
    val id = request.params.apply("id")
    val outcome = for {
      job_? <- jobClient.getJobByID(id)
      (job, success) <- job_? match {
        case Some(job) => jobTracker.stopJob(job).map(job -> _)
        case None => Future.failed(js.JavaScriptException(s"job $id not found"))
      }
    } yield (job, success)

    outcome onComplete {
      case Success((job, success)) =>
        if (success) job.state = JobStates.STOPPED
        job.message = "Stopped by user"
        response.send(job)
        next()
      case Failure(e) => response.internalServerError(e); next()
    }
  })

}