package com.github.ldaniels528.transgress.worker.devices.sources

import com.github.ldaniels528.transgress.worker.models.Field

/**
  * Represents a MongoDB Source
  * @author lawrence.daniels@gmail.com
  */
case class MongoSource(name: String,
                       url: String,
                       collection: String,
                       `type`: String,
                       format: String,
                       columnHeaders: Boolean,
                       fields: Seq[Field]) extends Source {

  def path: String = url

}