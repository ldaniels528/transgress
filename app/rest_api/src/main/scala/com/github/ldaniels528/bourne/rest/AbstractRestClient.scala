package com.github.ldaniels528.bourne.rest

import com.github.ldaniels528.bourne.rest.LoggerFactory.Logger
import io.scalajs.npm.request.{RequestOptions, Request => Client}
import io.scalajs.{JSON, RawOptions}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js.|

/**
  * Abstract REST Client
  * @author lawrence.daniels@gmail.com
  */
class AbstractRestClient(endpoint: String) {
  protected val logger: Logger = LoggerFactory.getLogger(getClass)

  def get[T](uri: String)(implicit ec: ExecutionContext): Future[T] = {
    Client.getAsync(url = getUrl(uri)).future map {
      case (response, body) => JSON.parseAs[T](body)
    }
  }

  def patch[T](options: RequestOptions)(implicit ec: ExecutionContext): Future[T] = {
    Client.patchAsync(options).future map {
      case (response, body) => JSON.parseAs[T](body)
    }
  }

  def patch[T](uri: String)(implicit ec: ExecutionContext): Future[T] = {
    Client.patchAsync(url = getUrl(uri)).future map {
      case (response, body) => JSON.parseAs[T](body)
    }
  }

  def post[T](uri: String, options: RequestOptions | RawOptions)(implicit ec: ExecutionContext): Future[T] = {
    Client.postAsync(uri, options).future map {
      case (response, body) => JSON.parseAs[T](body)
    }
  }

  def post[T](options: RequestOptions)(implicit ec: ExecutionContext): Future[T] = {
    Client.postAsync(options).future map {
      case (response, body) => JSON.parseAs[T](body)
    }
  }

  def post[T](uri: String)(implicit ec: ExecutionContext): Future[T] = {
    Client.postAsync(url = getUrl(uri)).future map {
      case (response, body) => JSON.parseAs[T](body)
    }
  }

  protected def getUrl(uri: String): String = {
    val url = s"http://$endpoint/api/$uri"
    logger.info(s"request: $url")
    url
  }

}
