package com.github.ldaniels528.transgress.rest

import com.github.ldaniels528.transgress.models.SlaveLike
import io.scalajs.npm.request.RequestOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Slave REST Client
  * @author lawrence.daniels@gmail.com
  */
class SlaveClient(endpoint: String) extends AbstractRestClient(endpoint) {

  def upsertSlave(slave: Slave)(implicit ec: ExecutionContext): Future[Slave] = {
    post[Slave](new RequestOptions(uri = getUrl("slave"), json = slave))
  }

}

@ScalaJSDefined
class Slave(val _id: js.UndefOr[String] = js.undefined,
            val name: js.UndefOr[String] = js.undefined,
            val host: js.UndefOr[String],
            val port: js.UndefOr[String],
            val maxConcurrency: js.UndefOr[Int],
            var concurrency: js.UndefOr[Int],
            var lastUpdated: js.UndefOr[js.Date] = js.undefined) extends SlaveLike