package com.github.ldaniels528.bourne.dao

import io.scalajs.npm.mongodb.Collection

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



  }

}