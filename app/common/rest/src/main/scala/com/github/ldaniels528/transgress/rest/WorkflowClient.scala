package com.github.ldaniels528.transgress.rest

import com.github.ldaniels528.transgress.models.WorkflowLike

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
    get[js.Array[WorkflowLike]](s"workflows")
  }

  /**
    * Retrieves a workflow by name
    * @param name the name of the workflow to search for
    * @return the promise of the option of a workflow
    */
  def findByName(name: String)(implicit ec: ExecutionContext): Future[Option[WorkflowLike]] = {
    get[js.Array[WorkflowLike]](s"workflow?name=$name") map (_.headOption)
  }

}
