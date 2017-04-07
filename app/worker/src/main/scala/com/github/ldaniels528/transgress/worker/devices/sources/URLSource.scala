package com.github.ldaniels528.transgress.worker.devices.sources

import com.github.ldaniels528.transgress.worker.models.Field

/**
  * Represents a URL Source
  * @author lawrence.daniels@gmail.com
  */
case class URLSource(name: String,
                     var path: String,
                     `type`: String,
                     format: String,
                     columnHeaders: Boolean,
                     fields: Seq[Field]) extends Source