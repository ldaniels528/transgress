package com.github.ldaniels528.bourne.worker.routes

import com.github.ldaniels528.bourne.dao.JobDAO._
import com.github.ldaniels528.bourne.models.JobStates
import io.scalajs.npm.express.{Application, Request, Response}
import io.scalajs.npm.mongodb.Db

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
  * Job Routes
  * @author lawrence.daniels@gmail.com
  */
class JobRoutes(app: Application)(implicit db: Db, ec: ExecutionContext) {
  private val jobDAO = db.getJobDAO

  ///////////////////////////////////////////////////////////////
  //    Routes
  ///////////////////////////////////////////////////////////////

  app.get("/api/jobs", (request: Request, response: Response, next: NextFunction) => {
    jobDAO.findByState(JobStates.values.toSeq: _*).toArray().toFuture onComplete {
      case Success(jobs) =>
        response.send(jobs)
        next()
      case Failure(e) =>
        response.internalServerError(e.getMessage)
        next()
    }
  })

}