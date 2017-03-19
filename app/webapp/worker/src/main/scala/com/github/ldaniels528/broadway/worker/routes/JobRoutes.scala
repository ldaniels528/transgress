package com.github.ldaniels528.broadway.worker.routes

import com.github.ldaniels528.broadway.worker.JobProcessor
import io.scalajs.npm.express.{Application, Request, Response}
import io.scalajs.npm.mongodb.Db

import scala.concurrent.ExecutionContext
import scala.scalajs.js.JSConverters._

/**
  * Job Routes
  * @author lawrence.daniels@gmail.com
  */
class JobRoutes(app: Application, jobProcessor: JobProcessor, db: Db)(implicit ec: ExecutionContext) {

  ///////////////////////////////////////////////////////////////
  //    Routes
  ///////////////////////////////////////////////////////////////

  app.get("/api/jobs", (request: Request, response: Response, next: NextFunction) => {
    listJobs(request, response, next)
  })

  ///////////////////////////////////////////////////////////////
  //    Methods
  ///////////////////////////////////////////////////////////////

  def listJobs(request: Request, response: Response, next: NextFunction) {
    response.send(jobProcessor.getJobs.toJSArray)
    next()
  }

}