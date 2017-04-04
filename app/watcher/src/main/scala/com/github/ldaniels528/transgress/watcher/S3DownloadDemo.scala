package com.github.ldaniels528.transgress.watcher

import S3DownloadDemo._
import com.github.ldaniels528.transgress.EnvironmentHelper._
import io.scalajs.JSON
import io.scalajs.nodejs.fs.Fs
import io.scalajs.nodejs.path.Path
import io.scalajs.nodejs.{console, process}
import io.scalajs.npm.aws._
import io.scalajs.npm.aws.s3._
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.{Future, Promise}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * S3 Download Demo
  * @author lawrence.daniels@gmail.com
  */
class S3DownloadDemo {

  def run(): Unit = {
    val params = new AppParams(
      filename = "test_data/COMPETITIVE_DEALERS_20170201.csv",
      cfg = new AppConfig(
        access_key_id = process.awsAccessKeyID,
        secret_access_key = process.awsSecretAccessKey,
        region = "us-west-2",
        bucket = "awsmeinp.data.test",
        prefix = "test_data/",
        task_definitions_idx = js.Dictionary(
          "ON_THE_LOT_" -> new DataPath(data_path = "."),
          "COMPETITIVE_DEALERS_" -> new DataPath(data_path = ".")
        )))

    for {
      files <- listS3Files(params)
      _ = console.log("files = ", JSON.stringify(files, null, 4))
      _ = params.filename = files(0)

      result <- getS3File(params)
      _ = console.log("result = ", JSON.stringify(result, null, 4))
    } {
      console.log("Done.")
    }
  }

  def listS3Files(params: AppParams): Future[js.Array[String]] = {
    val s3 = getS3(params)
    val cfg = params.cfg

    for {
      response <- s3.listObjectsV2(new ListObjectsV2Request(Bucket = cfg.bucket, Prefix = cfg.prefix)).promise()
      files = response.Contents
        .filter(!_.Key.endsWith("/"))
        .map(v => if (v.Key.indexOf("/") != -1) v.Key.substring(v.Key.indexOf("/") + 1) else v.Key)
        .sort((a, b) => a.compareTo(b))
        .reverse
    } yield files
  }

  def getS3File(params: AppParams): Future[Unit] = {
    val promise = Promise[Unit]()
    val cfg = params.cfg
    val bucket = cfg.bucket
    val prefix = if (cfg.prefix.endsWith("/")) cfg.prefix else cfg.prefix + "/"

    // e.g. "COMPETITIVE_DEALERS_20170201.csv"
    val filename = params.filename

    // e.g. "test_data/COMPETITIVE_DEALERS_20170201.csv"
    val filepath = prefix + filename

    // get the target path
    val data_path = cfg.task_definitions_idx(baseFilename(filename)).data_path

    val s3 = getS3(params)
    val pathObj = Path.parse(filename)
    val targetFile = (if (data_path.endsWith("/")) data_path else data_path + "/") + pathObj.name + pathObj.ext
    val stream = s3.getObject(new GetObjectRequest(Bucket = bucket, Key = filepath)).createReadStream()
    val out = Fs.createWriteStream(targetFile)
    stream.pipe(out)

    stream.on("finish", () => promise.success({}))
    stream.onError(err => promise.failure(js.JavaScriptException(err.message)))
    promise.future
  }

  private def getS3(params: AppParams) = {
    params.s3 getOrElse {
      val cfg = params.cfg
      val config = new ClientConfiguration()
      config.accessKeyId = cfg.access_key_id
      config.secretAccessKey = cfg.secret_access_key
      config.region = cfg.region ?? "us-west-2"
      val s3 = new AWS.S3(config)
      params.s3 = s3
      s3
    }
  }

  private def baseFilename(file: String): String = {
    val regex = """(\S+)[2][0]\d{2}\d{2}\d{2}[.]\S{1,3}$""".r
    file match {
      case regex(basename) => basename
      case _ => file
    }
  }

}

/**
  * S3 Download Demo
  * @author lawrence.daniels@gmail.com
  */
object S3DownloadDemo {

  @ScalaJSDefined
  class AppParams(val cfg: AppConfig,
                  var filename: String,
                  var s3: js.UndefOr[AWS.S3] = js.undefined) extends js.Object

  @ScalaJSDefined
  class AppConfig(val access_key_id: String,
                  val secret_access_key: String,
                  val bucket: String,
                  val prefix: String,
                  val task_definitions_idx: js.Dictionary[DataPath],
                  val region: js.UndefOr[String] = js.undefined) extends js.Object

  @ScalaJSDefined
  class DataPath(val data_path: String) extends js.Object

}