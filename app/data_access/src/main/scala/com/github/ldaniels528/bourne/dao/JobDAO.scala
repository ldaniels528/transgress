package com.github.ldaniels528.bourne.dao

import com.github.ldaniels528.bourne.models.JobStates.JobState
import com.github.ldaniels528.bourne.models.{JobStates, StatisticsLike}
import io.scalajs.npm.mongodb._

import scala.scalajs.js
import scala.scalajs.js.Promise

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

    def checkoutJob(host: String): Promise[FindAndModifyWriteOpResult] = {
      dao.findOneAndUpdate(
        filter = doc("state" $eq JobStates.NEW),
        update = doc($set("state" -> JobStates.QUEUED, "processingHost" -> host)),
        options = new FindAndUpdateOptions(sort = doc("priority" -> 1), returnOriginal = false)
      )
    }

    @inline
    def createJob(job: JobData): js.Promise[InsertWriteOpResult] = {
      val dict = job.asInstanceOf[js.Dictionary[js.Any]]
      dict("lastUpdated") = js.Date.now()
      dao.insertOne(dict, new WriteOptions())
    }

    @inline
    def changeState(jobId: String, state: JobState, message: js.UndefOr[String]): js.Promise[FindAndModifyWriteOpResult] = {
      dao.findOneAndUpdate(
        filter = doc("_id" $eq new ObjectID(jobId)),
        update = doc($set("state" -> state, "message" -> message, "lastUpdated" -> js.Date.now())),
        options = new FindAndUpdateOptions(returnOriginal = false)
      )
    }

    @inline
    def findByState(states: JobState*): Cursor[JobData] = {
      dao.find[JobData](doc("state" $in js.Array(states.map(_.toString): _*))).sort("priority", 1)
    }

    @inline
    def updateJob(job: JobData): js.Promise[UpdateWriteOpResultObject] = {
      val dict = job.asInstanceOf[js.Dictionary[js.Any]]
      dict("lastUpdated") = js.Date.now()
      dao.updateOne("_id" $eq job._id, dict)
    }

    @inline
    def updateStatistics(id: String, statistics: StatisticsLike): js.Promise[FindAndModifyWriteOpResult] = {
      dao.findOneAndUpdate(
        filter =  "_id" $eq new ObjectID(id),
        update = doc($set("statistics" -> statistics, "lastUpdated" -> js.Date.now())),
        options = new FindAndUpdateOptions(returnOriginal = false)
      )
    }

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
