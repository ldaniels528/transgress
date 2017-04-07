package com.github.ldaniels528.transgress.worker.devices.formats

/**
  * Pipe Separated Values (PSV) Format
  * @author lawrence.daniels@gmail.com
  */
class PSVFormat(columnHeaders: Boolean = true) extends DelimitedFormat(delimiter =  "|", columnHeaders)