package com.github.ldaniels528.bourne
package server
package routes

import com.github.ldaniels528.bourne.dao.JobDAO._
import com.github.ldaniels528.bourne.dao.JobData
import com.github.ldaniels528.bourne.models.{JobStates, StatisticsLike}
import io.scalajs.JSON
import io.scalajs.nodejs.console
import io.scalajs.npm.express.{Application, Request, Response}
import io.scalajs.npm.expressws.WsRouting
import io.scalajs.npm.mongodb._

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Job Routes
  * @author lawrence.daniels@gmail.com
  */
class JobRoutes(app: Application with WsRouting, db: Db)(implicit ec: ExecutionContext) {
  private val jobDAO = db.getJobDAO

  /**
    * Retrieves a job by ID
    */
  app.get("/api/job/:id", (request: Request, response: Response, next: NextFunction) => {
    val id = request.params.apply("id")
    jobDAO.findOneAsync[JobData](doc("_id" $eq new ObjectID(id))) onComplete {
      case Success(Some(job)) => response.send(js.Array(job)); next()
      case Success(None) => response.send(js.Array()); next()
      case Failure(e) => response.internalServerError(e)
    }
  })

  /**
    * Change a job's state
    */
  app.patch("/api/job/:id/state/:state", (request: Request, response: Response, next: NextFunction) => {
    val (id, state) = (request.params.apply("id"), request.params.apply("state"))
    val outcome = jobDAO.changeState(id, state).toFuture
    outcome onComplete {
      case Success(result) if result.isOk && result.value != null => response.send(js.Array(result.value)); next()
      case Success(result) => response.send(js.Array()); next()
      case Failure(e) => response.internalServerError(e)
    }
  })

  /**
    * Updates job statistics
    */
  app.patch("/api/job/:id/statistics", (request: Request, response: Response, next: NextFunction) => {
    val jobId = request.params.apply("id")
    val statistics = request.bodyAs[StatisticsLike]
    val outcome = jobDAO.updateStatistics(jobId, statistics).toFuture
    outcome onComplete {
      case Success(result) if result.nModified == 1 => response.send(result); next()
      case Success(result) => response.notFound(s"Job statistics not updated: ${JSON.stringify(result)}")
      case Failure(e) => response.internalServerError(e)
    }
  })

  /**
    * Retrieve jobs by state
    */
  app.get("/api/jobs", (request: Request, response: Response, next: NextFunction) => {
    jobDAO.findByState(JobStates.values.toSeq: _*).toArray().toFuture onComplete {
      case Success(jobs) =>
        response.send(jobs)
        next()
      case Failure(e) =>
        response.internalServerError(e)
    }
  })

  /**
    * Creates a new job
    */
  app.post("/api/jobs", (request: Request, response: Response, next: NextFunction) => {
    val job = request.bodyAs[JobData]
    val outcome = jobDAO.createJob(job).toFuture
    outcome onComplete {
      case Success(result) if result.insertedCount == 1 =>
        console.info(s"/api/jobs [out] result = ${JSON.stringify(result.ops)}")
        response.send(result); next()
      case Success(result) =>
        console.log(s"result = ${JSON.stringify(result)}")
        response.badRequest(s"Job not created: ${JSON.stringify(result)}")
      case Failure(e) => response.internalServerError(e); next()
    }
  })

  /**
    * Retrieves the next job from the queue
    */
  app.patch("/api/jobs/checkout/:host", (request: Request, response: Response, next: NextFunction) => {
    val host = request.params.apply("host")
    val outcome = jobDAO.findOneAndUpdate(
      filter = doc("state" $eq JobStates.NEW),
      update = doc($set("state" -> JobStates.QUEUED, "processingHost" -> host)),
      options = new FindAndUpdateOptions(sort = doc("priority" -> 1), returnOriginal = false)
    ).toFuture
    outcome onComplete {
      case Success(result) if result.isOk && result.value != null => response.send(js.Array(result.value)); next()
      case Success(result) => response.send(js.Array()); next()
      case Failure(e) => response.internalServerError(e)
    }
  })

}
