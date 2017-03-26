package com.github.ldaniels528.bourne.rest

import com.github.ldaniels528.bourne.models.WorkflowLike
import io.scalajs.JSON
import io.scalajs.npm.request.{Request => Client}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * Workflow REST Client
  * @author lawrence.daniels@gmail.com
  */
class WorkflowClient(endpoint: String) extends AbstractRestClient(endpoint) {

  /**
    * Retrieves all workflows
    * @return the promise of an array of a workflows
    */
  def findAll()(implicit ec: ExecutionContext): Future[js.Array[WorkflowLike]] = {
    Client.getAsync(getUrl(s"workflows")).future.map {
      case (_, body) => JSON.parseAs[js.Array[WorkflowLike]](body)
    }
  }

  /**
    * Retrieves a workflow by name
    * @param name the name of the workflow to search for
    * @return the promise of the option of a workflow
    */
  def findByName(name: String)(implicit ec: ExecutionContext): Future[Option[WorkflowLike]] = {
    Client.getAsync(getUrl(s"workflow?name=$name")).future.map {
      case (_, body) =>
        logger.info(s"body => '$name' => $body")
        JSON.parseAs[js.Array[WorkflowLike]](body)
    } map (_.headOption)
  }

}
