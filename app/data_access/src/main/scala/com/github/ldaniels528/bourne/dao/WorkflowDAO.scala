package com.github.ldaniels528.bourne.dao

import io.scalajs.npm.mongodb._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * Workflow DAO
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait WorkflowDAO extends Collection

/**
  * Workflow DAO Companion
  * @author lawrence.daniels@gmail.com
  */
object WorkflowDAO {

  /**
    * Workflow DAO enrichment
    * @param dao the given [[WorkflowDAO data access object]]
    */
  final implicit class WorkflowDAOEnrichment(val dao: WorkflowDAO) extends AnyVal {

    @inline
    def findAll()(implicit ec: ExecutionContext): Future[js.Array[WorkflowData]] = {
      dao.find[WorkflowData]().toArray().toFuture
    }

    @inline
    def findByName(name: String)(implicit ec: ExecutionContext): Future[Option[WorkflowData]] = {
      dao.findOneAsync[WorkflowData](doc("name" $eq name))
    }

  }

  /**
    * Workflow DAO Constructor
    * @param db the given [[Db database]] instance
    */
  final implicit class WorkflowDAOConstructor(val db: Db) extends AnyVal {

    @inline
    def getWorkflowDAO: WorkflowDAO = db.collection("workflows").asInstanceOf[WorkflowDAO]

  }

}