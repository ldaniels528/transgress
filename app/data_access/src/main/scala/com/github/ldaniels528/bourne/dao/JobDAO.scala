package com.github.ldaniels528.bourne.dao

import com.github.ldaniels528.bourne.models.JobStates.JobState
import com.github.ldaniels528.bourne.models.{Job, JobStates, StatisticsLike}
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
    def createJob(job: JobData): js.Promise[InsertWriteOpResult] = {
      dao.insertOne(job, new WriteOptions())
    }

    @inline
    def changeState(jobId: String, state: JobState): js.Promise[FindAndModifyWriteOpResult] = {
      dao.findOneAndUpdate(
        filter = doc("_id" $eq new ObjectID(jobId)),
        update = doc("state" $set state),
        options = new FindAndUpdateOptions(sort = doc("priority" -> 1), returnOriginal = false)
      )
    }

    @inline
    def findByState(states: JobState*): Cursor[JobData] = {
      dao.find[JobData](doc("state" $in js.Array(states.map(_.toString): _*)))
    }

    @inline
    def updateJob(job: JobData): js.Promise[UpdateWriteOpResultObject] = {
      dao.updateOne("_id" $eq job._id, job)
    }

    @inline
    def updateStatistics(id: String, statistics: StatisticsLike): js.Promise[UpdateWriteOpResultObject] = {
      dao.updateOne("_id" $eq new ObjectID(id), doc("statistics" $set statistics))
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
      data._id = new ObjectID(job._id)
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
      _id = (jobData._id getOrElse new ObjectID()).toHexString(),
      name = jobData.name getOrElse "Unknown",
      input = jobData.input getOrElse "Unknown",
      workflowConfig = jobData.workflowConfig getOrElse "Unknown",
      state = jobData.state getOrElse JobStates.NEW,
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
