package com.github.ldaniels528.broadway.worker

import LoaderUtilities._
import io.scalajs.nodejs._
import io.scalajs.nodejs.buffer.Buffer
import io.scalajs.nodejs.fs.Fs
import io.scalajs.nodejs.zlib.Zlib
import io.scalajs.npm.csvtojson
import io.scalajs.npm.csvtojson.ConverterOptions
import io.scalajs.npm.moment.Moment
import io.scalajs.npm.mongodb._
import io.scalajs.npm.throttle._
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.language.{existentials, reflectiveCalls}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.{Failure, Success}

/**
  * Listing Activity Processor
  */
class ListingActivityProcessor(db: Db, coll: Collection, options: ProcessingOptions) {
  private val batchSize = 100
  private val writeOptions = new WriteOptions(ordered = false)

  // parsing & batch related variables
  private var active = 0
  private var batch = js.Array[DataObject]()
  private var failures = 0
  private var parsingCompleted = false

  /**
    * Executes the ETL process
    * @return a completion promise
    */
  def execute()(implicit statsGen: StatisticsGenerator): Future[Long] = {
    // open the file for reading
    console.log(s"Opening input file '${options.filename}'...")
    val fileStream = options.filename match {
      case s if s.endsWith(".gz") => Fs.createReadStream(s).pipe(Zlib.createGunzip())
      case s => Fs.createReadStream(s)
    }

    // update the statistics
    fileStream.onData[Buffer](statsGen.bytesRead += _.length)

    // start the logger
    val loggerInterval = setInterval(() => logProgress(), 5.seconds)

    // setup the CSV-to-JSON conversion
    val converter = new csvtojson.Converter(new ConverterOptions(
      constructResult = false,
      delimiter = "|"
    )).on("record_parsed", (data: DataObject) => handleItem(data))
      .on("end_parsed", (data: DataObject) => handleLastItem(data))
      .on("error", (err: Error) => console.error("converter:", err))

    // pipe the file's content to the converter
    if (isPeakHours || options.useThrottling) {
      console.info("Throttling %.1f KB/second", options.throttleRate)
      fileStream.pipe(new Throttle(options.throttleRate * 1024)).pipe(converter)
    }
    else {
      fileStream.pipe(converter)
    }

    // orchestrate the process
    for {
      _ <- awaitParsingCompletion()
      _ <- awaitDatabaseWrites()
      _ = clearInterval(loggerInterval)
      _ <- createIndex()
      _ <- renameCollection(maxRetries = 20)
      _ <- fileStream.closeAsync
    } yield statsGen.totalInserted
  }

  private def handleItem(data: DataObject)(implicit statsGen: StatisticsGenerator) = {
    // add the timezone
    data.MODIFIED_DATE = data.MODIFIED_DATE.map(_ + "-0500")
    batch.push(data)

    // add the data to the batch
    if (batch.length >= batchSize || active == 0) persistBatch()
    else Future.successful({})
  }

  private def handleLastItem(data: DataObject)(implicit statsGen: StatisticsGenerator) = {
    // last item is always an empty array ([])
    persistBatch() onceCompleted { _ =>
      parsingCompleted = true
    }
  }

  private def isPeakHours = false

  private def logProgress(force: Boolean = false)(implicit statsGen: StatisticsGenerator) {
    statsGen.update(force) foreach { stats =>
      console.log(f"${Moment(new js.Date()).format()}: $active active, $stats")
    }
  }

  private def persistBatch()(implicit statsGen: StatisticsGenerator): Future[Int] = {
    if (batch.nonEmpty) {
      // persist the batch
      val batchRef = batch
      batch = js.Array()
      active += 1
      coll.insertMany(batchRef, writeOptions).map(_.insertedCount) onceCompleted {
        case Success(inserted) =>
          active -= 1
          statsGen.totalInserted += inserted
          logProgress()
        case Failure(e) =>
          active -= 1
          console.error(e.getMessage)
          failures += batchRef.length
      }
    }
    else Future.successful(0)
  }

  private def awaitCondition(message: String)(condition: => Boolean): Future[Unit] = {
    val promise = Promise[Unit]()

    def checkForCompletion() {
      if (!condition) {
        setTimeout(() => checkForCompletion(), 5.seconds)
      }
      else promise.success({})
    }

    console.log(message)
    setTimeout(() => checkForCompletion(), 1.second)
    promise.future
  }

  private def awaitDatabaseWrites()(implicit statsGen: StatisticsGenerator) = {
    awaitCondition("Waiting for database writes to complete...")(parsingCompleted && active == 0) onceCompleted { _ =>
      logProgress(force = true)
    }
  }

  private def awaitParsingCompletion() = {
    awaitCondition("Waiting for parsing to complete...")(parsingCompleted)
  }

  private def createIndex() = {
    console.log("Creating Index...")
    val options = new IndexOptions(background = true, v = 1)
    coll.ensureIndex(doc("OWNER_ID" -> 1, "CAR_ID" -> 1, "MODIFIED_DATE:" -> -1), options) onceCompleted {
      case Success(_) => console.log(s"Index created.")
      case Failure(e) => console.error(s"Error creating index: ${e.getMessage}")
    }
  }

  private def renameCollection(maxRetries: Int) = {
    val promise = Promise[Int]()

    def attemptRename(attempts: Int = 1) {
      coll.rename(options.collectionName, new RenameOptions(dropTarget = true)) onComplete {
        case Success(_) =>
          promise.success(attempts)
          if (attempts > 1) console.log(s"Rename collection succeeded after $attempts tries.")
          console.log("Rename collection succeeded.")

        case Failure(e) =>
          if (attempts < maxRetries) {
            setTimeout(() => attemptRename(attempts + 1), 60.seconds)
          } else {
            promise.failure(e)
            console.error(s"Rename collection failed: ${e.getMessage}")
            console.warn(s"Rename failed after $attempts attempts!")
          }
      }
    }

    console.log(s"Renaming collection to '${options.throttleRate}'...")
    attemptRename()
    promise.future
  }

}

@ScalaJSDefined
class DataObject(var MODIFIED_DATE: js.UndefOr[String] = js.undefined) extends js.Object
