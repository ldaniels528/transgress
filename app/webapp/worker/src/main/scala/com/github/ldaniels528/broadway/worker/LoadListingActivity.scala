package com.github.ldaniels528.broadway.worker

import io.scalajs.nodejs._
import io.scalajs.nodejs.fs.Fs
import io.scalajs.npm.mongodb._
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success}

/**
  * Load Listing Activity
  */
object LoadListingActivity extends js.JSApp {
  private val version = "0.1.1"
  private val listOfAllowedCollections = js.Array("listing_activity")

  @JSExport
  override def main() {
    console.log(s"LoadListingActivity v$version")

    // setup the process
    val options = getCommandLineArguments

    // ensure we're authorized to process this collection
    validateCollection(options.collectionName)

    // handle uncaught exceptions
    process.onUncaughtException(err => {
      console.error("An expected error occurred", err)
      process.exit(3)
    })

    // did we finish cleanly?
    process.onExit(exitCode => {
      console.info(s"Process ended: exit code = $exitCode")
    })

    // capture the start time of the process
    val startTime = js.Date.now()

    // let's define a function to compute the elapsed time
    def elapsedTime = (js.Date.now() - startTime).milliseconds.toSeconds

    // run the process
    val outcome = for {
      stats <- Fs.statAsync(options.filename)
      statsGen = new StatisticsGenerator(stats.size)
      totalInserted <- run(options, statsGen)
    } yield totalInserted

    outcome onComplete {
      case Success(total) =>
        console.log(s"Processed $total record(s) in $elapsedTime sec(s)")
      case Failure(e) =>
        console.error(s"Failed after $elapsedTime sec(s): ${e.getMessage}")
        e.printStackTrace()
    }
  }

  def run(options: ProcessingOptions, statsGen: StatisticsGenerator): Future[Long] = {
    val collectionNameNew = options.collectionName + "_new"
    console.log(s"""Loading "${options.collectionName}" collection as "$collectionNameNew"""")

    for {
      db <- connectToDatabase(collectionNameNew)
      coll = db.collection(collectionNameNew)
      _ <- dropCollectionIfExists(collectionNameNew, db, coll)
      totalInserted <- new ListingActivityProcessor(db, coll, options).execute()(statsGen)
      _ <- db.close()
    } yield totalInserted
  }

  private def connectToDatabase(collectionName: String): Future[Db] = {
    val mongoHosts = process.env.getOrElse("MONGO_HOSTS", "localhost:27017")
    val mongoConnect = s"mongodb://$mongoHosts/mi"
    console.log(s"Connecting to '$mongoConnect' ($collectionName)...")
    MongoClient.connectAsync(mongoConnect)
  }

  private def dropCollectionIfExists(collectionName: String, db: Db, coll: Collection) = {
    for {
      collections <- db.listCollections().toArray().map(_ map (_.name))
      dropped <- {
        if (collections.contains(collectionName)) {
          console.log(s"Dropping collection '$collectionName'...")
          coll.drop().map(_ => true)
        }
        else Future.successful(false)
      }
    } yield dropped
  }

  private def getCommandLineArguments: ProcessingOptions = {
    process.argv.drop(2).toList match {
      case file :: collection :: isThrottled :: throttleRate :: Nil =>
        ProcessingOptions(file, collection, isThrottled.toLowerCase() == "true", throttleRate.toDouble)
      case file :: collection :: isThrottled :: Nil =>
        ProcessingOptions(file, collection, isThrottled.toLowerCase() == "true")
      case file :: collection :: Nil => ProcessingOptions(file, collection)
      case file :: Nil => ProcessingOptions(file, "listing_activity")
      case Nil if process.env.contains("LOCAL_DEV") =>
        ProcessingOptions("LISTING_ACTIVITY_20170209.txt", "listing_activity", useThrottling = false)
      case _ =>
        console.error(s"USAGE: ${getClass.getSimpleName} <CSV> <Mongo collection>\n")
        console.error("WARNING: The Mongo collection specified will be dropped before it is populated! \n")
        process.exit(1)
        null
    }
  }

  private def validateCollection(collectionName: String) = {
    if (!listOfAllowedCollections.contains(collectionName)) {
      console.error(s"""The collection "$collectionName" is not in the whitelist.""")
      process.exit(2)
    }
  }

}
