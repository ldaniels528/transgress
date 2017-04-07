package com.github.ldaniels528.transgress.worker.devices.formats

/**
  * Data Format Factory
  * @author lawrence.daniels@gmail.com
  */
object DataFormatFactory {

  def getFormat(format: String): Option[DataFormat] = {
    format match {
      case "csv" => Option(new CSVFormat())
      case "fixed" => Option(new FixedLengthFormat(Nil))
      case "json" => Option(new JSONFormat())
      case "psv" => Option(new PSVFormat())
      case "tsv" => Option(new TSVFormat())
      case _ => None
    }
  }

}
