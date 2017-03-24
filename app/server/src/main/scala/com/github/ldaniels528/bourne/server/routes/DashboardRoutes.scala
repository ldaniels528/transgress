package com.github.ldaniels528.bourne.server.routes

import com.github.ldaniels528.bourne.models.Job
import com.github.ldaniels528.bourne.rest.LoggerFactory
import io.scalajs.JSON
import io.scalajs.npm.express.{Application, Request, Response}
import io.scalajs.npm.expressws.WsRouting
import io.scalajs.npm.mongodb.Db
import io.scalajs.npm.request.{Request => Client}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * Dashboard Routes
  * @author lawrence.daniels@gmail.com
  */
class DashboardRoutes(app: Application with WsRouting, db: Db)(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val workers = Seq("localhost:1337")

  ///////////////////////////////////////////////////////////////
  //    Routes
  ///////////////////////////////////////////////////////////////

  app.get("/api/jobs", (request: Request, response: Response, next: NextFunction) =>
    listJobs(request, response, next))

  ///////////////////////////////////////////////////////////////
  //    Methods
  ///////////////////////////////////////////////////////////////

  def listJobs(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext): Unit = {
    val tasks = Future.sequence(workers map { worker =>
      val url = s"http://$worker/api/jobs"
      logger.info(s"Connect to url '$url'...")
      Client.getFuture(s"http://$worker/api/jobs").future.map {
        case (_, body) => JSON.parseAs[js.Array[Job]](body).toSeq
      }
    }) map (_.flatten.toJSArray)
    tasks onComplete {
      case Success(results) =>
        response.send(results)
        next()
      case Failure(e) =>
        response.internalServerError(e)
        next()
    }
  }

}
