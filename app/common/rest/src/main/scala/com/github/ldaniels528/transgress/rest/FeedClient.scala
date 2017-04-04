package com.github.ldaniels528.transgress.rest

import com.github.ldaniels528.transgress.models.FeedLike
import io.scalajs.npm.request.RequestOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Feed REST Client
  * @author lawrence.daniels@gmail.com
  */
class FeedClient(endpoint: String) extends AbstractRestClient(endpoint) {

  def upsertFeed(feed: Feed)(implicit ec: ExecutionContext): Future[Feed] = {
    post[Feed](new RequestOptions(uri = getUrl("feed"), json = feed))
  }

}

/**
  * Represents a feed document
  * @param _id      the MongoDB identifier
  * @param filename the feed filename
  * @param mtime    the feed last modified time
  * @param size     the feed size
  */
@ScalaJSDefined
class Feed(val _id: js.UndefOr[String] = js.undefined,
           val filename: js.UndefOr[String],
           val mtime: js.UndefOr[Double],
           val size: js.UndefOr[Double],
           val createdTime: js.UndefOr[js.Date] = js.undefined)
  extends FeedLike