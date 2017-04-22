package com.github.ldaniels528.transgress.rest

import com.github.ldaniels528.transgress.LoggerFactory
import com.github.ldaniels528.transgress.LoggerFactory.Logger
import io.scalajs.npm.request.{RequestBody, RequestOptions, Request => Client}
import io.scalajs.{JSON, RawOptions}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.|

/**
  * Abstract REST Client
  * @author lawrence.daniels@gmail.com
  */
class AbstractRestClient(endpoint: String) {
  protected val logger: Logger = LoggerFactory.getLogger(getClass)

  def get[T](uri: String)(implicit ec: ExecutionContext): Future[T] = {
    Client.getFuture(url = getUrl(uri)) map {
      case (_, body) => parseBody[T](body)
    }
  }

  def patch[T](options: RequestOptions)(implicit ec: ExecutionContext): Future[T] = {
    Client.patchFuture(options) map {
      case (_, body) => parseBody[T](body)
    }
  }

  def patch[T](uri: String)(implicit ec: ExecutionContext): Future[T] = {
    Client.patchFuture(url = getUrl(uri)) map {
      case (_, body) => parseBody[T](body)
    }
  }

  def post[T](uri: String, options: RequestOptions | RawOptions)(implicit ec: ExecutionContext): Future[T] = {
    Client.postFuture(uri, options) map {
      case (_, body) => parseBody[T](body)
    }
  }

  def post[T](options: RequestOptions)(implicit ec: ExecutionContext): Future[T] = {
    Client.postFuture(options) map {
      case (_, body) => parseBody[T](body)
    }
  }

  def post[T](uri: String)(implicit ec: ExecutionContext): Future[T] = {
    Client.postFuture(url = getUrl(uri)) map {
      case (_, body) => parseBody[T](body)
    }
  }

  protected def getUrl(uri: String): String = {
    val url = s"http://$endpoint/api/$uri"
    //logger.info(s"request: $url")
    url
  }

  private def parseBody[T](body: RequestBody) = {
    //logger.info(s"body => ${JSON.stringify(body)}")
    body match {
      case s if s.toString.startsWith("ERROR") => throw js.JavaScriptException(s)
      case s if js.typeOf(s) == "string" => JSON.parseAs[T](body.asInstanceOf[String])
      case o => o.asInstanceOf[T]
    }
  }

}
