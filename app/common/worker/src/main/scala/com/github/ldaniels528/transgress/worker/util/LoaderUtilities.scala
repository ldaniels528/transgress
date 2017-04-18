package com.github.ldaniels528.transgress.worker.util

import scala.annotation.tailrec

/**
  * Loader Utilities
  * @author lawrence.daniels@gmail.com
  */
object LoaderUtilities {
  private val UnitNames = Seq("Bytes", "KB", "MB", "GB", "TB")

  /**
    * Byte Unit Enrichment
    * @param value the given byte size
    */
  implicit class ByteUnitEnrichment(val value: Double) extends AnyVal {

    @inline
    def bytes: String = units(value)

    @inline
    def bps: String = units(value) + "/sec"

    @tailrec
    private def units(value: Double, unit: Int = 0): String = {
      if (value >= 1000) units(value / 1024, unit + 1) else f"$value%.2f ${UnitNames(unit)}"
    }

  }

}
