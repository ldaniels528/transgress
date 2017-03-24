package com.github.ldaniels528.bourne.worker.devices

import io.scalajs.npm.mongodb.{MongoClient, WriteOptions}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * MongoDB Output Device
  * @param mongoConnect the MongoDB connection string
  * @param collection   the MongoDB collection
  */
class MongoDBOutputDevice(mongoConnect: String, collection: String)(implicit ec: ExecutionContext)
  extends OutputDevice {
  private val dbFuture = MongoClient.connectAsync(mongoConnect).toFuture
  private val collFuture = dbFuture.map(_.collection(collection))
  private var batch = js.Array[js.Any]()
  private val writeOptions = new WriteOptions(ordered = false)

  override def close(): Future[Unit] = dbFuture.flatMap(_.close().toFuture.map(_ => ()))

  override def flush(): Future[Int] = {
    if (batch.nonEmpty) persistBatch(batch) else Future.successful(0)
  }

  override def write(data: js.Any): Future[Int] = {
    if (batch.push(data) >= 100) {
      val batchTemp = batch
      batch = js.Array[js.Any]()
      persistBatch(batchTemp)
    }
    else Future.successful(0)
  }

  private def persistBatch(aBatch: js.Array[js.Any]) = {
    collFuture.flatMap(_.insertMany(batch, writeOptions).toFuture.map(_.insertedCount))
  }

}
