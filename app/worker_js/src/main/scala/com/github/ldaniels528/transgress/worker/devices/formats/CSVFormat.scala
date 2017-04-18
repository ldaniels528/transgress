package com.github.ldaniels528.transgress.worker.devices.formats

/**
  * Comma Separated Values (CSV) Format
  * @author lawrence.daniels@gmail.com
  */
class CSVFormat(columnHeaders: Boolean = true) extends DelimitedFormat(delimiter =  ",", columnHeaders)