package com.github.ldaniels528.bourne.worker.routes

import com.github.ldaniels528.bourne.models.JobStates
import com.github.ldaniels528.bourne.worker.WorkerConfig
import com.github.ldaniels528.bourne.worker.rest.JobClient
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
  * Job Routes
  * @author lawrence.daniels@gmail.com
  */
class WorkerRoutes(app: Application)(implicit ec: ExecutionContext) {
  //private val jobDAO = new JobClient(config.master.orNull)

  ///////////////////////////////////////////////////////////////
  //    Routes
  ///////////////////////////////////////////////////////////////

  /*
  app.get("/api/jobs", (request: Request, response: Response, next: NextFunction) => {
    jobDAO.findByState(JobStates.values.toSeq: _*).toArray().toFuture onComplete {
      case Success(jobs) =>
        response.send(jobs)
        next()
      case Failure(e) =>
        response.internalServerError(e.getMessage)
        next()
    }
  })*/

}