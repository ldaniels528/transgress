package com.github.ldaniels528.transgress.worker.devices.formats

/**
  * Tab Separated Values (TSV) Format
  * @author lawrence.daniels@gmail.com
  */
class TSVFormat(columnHeaders: Boolean = true) extends DelimitedFormat(delimiter = "/t", columnHeaders)