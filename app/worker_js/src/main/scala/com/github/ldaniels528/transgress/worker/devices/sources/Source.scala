package com.github.ldaniels528.transgress.worker.devices
package sources

import com.github.ldaniels528.transgress.worker.models.Field

/**
  * Represents a Device Source
  * @author lawrence.daniels@gmail.com
  */
trait Source {

  def name: String

  def path: String

  def `type`: String

  def format: String

  def columnHeaders: Boolean

  def fields: Seq[Field]

}
