package com.github.ldaniels528.bourne.server.routes

import com.github.ldaniels528.bourne.dao.WorkflowDAO._
import com.github.ldaniels528.bourne.dao.WorkflowData
import io.scalajs.npm.express.{Application, Request, Response}
import io.scalajs.npm.expressws.WsRouting
import io.scalajs.npm.mongodb._

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Workflow Routes
  * @author lawrence.daniels@gmail.com
  */
class WorkflowRoutes(app: Application with WsRouting, db: Db)(implicit ec: ExecutionContext) {
  private val workflowDAO = db.getWorkflowDAO

  /**
    * Retrieves a workflow by name
    */
  app.get("/api/workflow", (request: Request, response: Response, next: NextFunction) => {
    request.query.get("name") match {
      case Some(name) =>
        workflowDAO.findOneAsync[WorkflowData](doc("name" $eq name)) onComplete {
          case Success(Some(workflows)) => response.send(js.Array(workflows)); next()
          case Success(None) => response.send(js.Array()); next()
          case Failure(e) => response.internalServerError(e)
        }
      case None =>
        response.badRequest("'name' is required")
    }
  })

  /**
    * Retrieves all workflows
    */
  app.get("/api/workflows", (request: Request, response: Response, next: NextFunction) => {
    workflowDAO.find[WorkflowData]().toArray().toFuture onComplete {
      case Success(workflows) => response.send(workflows); next()
      case Failure(e) => response.internalServerError(e)
    }
  })

}
