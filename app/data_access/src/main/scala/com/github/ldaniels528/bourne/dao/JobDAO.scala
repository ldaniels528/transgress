package com.github.ldaniels528.bourne.dao

import com.github.ldaniels528.bourne.models.JobStates.JobState
import com.github.ldaniels528.bourne.models.{Job, JobStates}
import io.scalajs.npm.mongodb._

import scala.scalajs.js

/**
  * Job DAO
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait JobDAO extends Collection

/**
  * JobDAO Companion
  * @author lawrence.daniels@gmail.com
  */
object JobDAO {

  /**
    * Job DAO enrichment
    * @param dao the given [[JobDAO data access object]]
    */
  final implicit class JobDAOEnrichment(val dao: JobDAO) extends AnyVal {

    @inline
    def create(job: JobData): js.Promise[InsertWriteOpResult] = dao.insertOne(job, new WriteOptions())

    @inline
    def changeState(job: JobData, state: JobState): js.Promise[UpdateWriteOpResultObject] = {
      dao.updateOne(filter = doc("_id" $eq job._id), update = doc("state" $set state.toString))
    }

    @inline
    def findByState(states: JobState*): Cursor[JobData] = {
      dao.find[JobData](doc("state" $in js.Array(states.map(_.toString): _*)))
    }

    def updateJob(job: JobData): js.Promise[UpdateWriteOpResultObject] = {
      dao.updateOne("_id" $eq job._id, job)
    }

  }

  /**
    * Job Model Enrichment
    * @param job the given [[Job job model]]
    */
  final implicit class JobModelEnrichment(val job: Job) extends AnyVal {

    @inline
    def toData: JobData = {
      val data = new js.Object().asInstanceOf[JobData]
      data._id = new ObjectID(job.id)
      data.name = job.name
      data.input = job.input
      data.workflowConfig = job.workflowConfig
      data.state = job.state.toString
      data.message = job.message
      data.statistics = job.statistics
      data
    }

  }

  /**
    * Job Data Enrichment
    * @param jobData the given [[JobData job document]]
    */
  final implicit class JobDataEnrichment(val jobData: JobData) extends AnyVal {

    @inline
    def toModel = new Job(
      id = (jobData._id getOrElse new ObjectID()).toHexString(),
      name = jobData.name getOrElse "Unknown",
      input = jobData.input getOrElse "Unknown",
      workflowConfig = jobData.workflowConfig getOrElse "Unknown",
      state = jobData.state.map(JobStates.withName) getOrElse JobStates.NEW,
      message = jobData.message,
      statistics = jobData.statistics
    )

  }

  /**
    * Job DAO Constructor
    * @param db the given [[Db database]] instance
    */
  final implicit class JobDAOConstructor(val db: Db) extends AnyVal {

    @inline
    def getJobDAO: JobDAO = db.collection("jobs").asInstanceOf[JobDAO]

  }

}
