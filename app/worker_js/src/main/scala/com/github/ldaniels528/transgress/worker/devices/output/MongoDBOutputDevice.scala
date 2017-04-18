package com.github.ldaniels528.transgress.worker.devices.output

import com.github.ldaniels528.transgress.worker.JobEventHandler
import com.github.ldaniels528.transgress.worker.devices.sources.MongoSource
import io.scalajs.npm.mongodb.{MongoClient, WriteOptions}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * MongoDB Output Device
  * @param source the [[MongoSource MongoDB Source]]
  */
class MongoDBOutputDevice(source: MongoSource)(implicit ec: ExecutionContext)
  extends OutputDevice {
  private val dbFuture = MongoClient.connectAsync(source.url).toFuture
  private val collFuture = dbFuture.map(_.collection(source.collection))
  private var batch = js.Array[js.Any]()
  private val writeOptions = new WriteOptions(ordered = false)

  override def close(): Future[Unit] = dbFuture.flatMap(_.close().toFuture.map(_ => ()))

  override def flush()(implicit jobEventHandler: JobEventHandler): Future[Int] = {
    if (batch.nonEmpty) persistBatch(batch) else Future.successful(0)
  }

  override def write(data: js.Any)(implicit jobEventHandler: JobEventHandler): Unit = {
    if (batch.push(data) >= 100) {
      val batchTemp = batch
      batch = js.Array[js.Any]()
      persistBatch(batchTemp)
    }
  }

  private def persistBatch(aBatch: js.Array[js.Any]) = {
    collFuture.flatMap(_.insertMany(batch, writeOptions).toFuture.map(_.insertedCount))
  }

}
