package com.github.ldaniels528.bourne.worker

/**
  * Processing Options
  * @param filename       the file to process
  * @param collectionName the collection to write to
  * @param useThrottling  indicates whether throttling should be used.
  * @param throttleRate   the throttling rate
  */
case class ProcessingOptions(filename: String,
                             collectionName: String,
                             useThrottling: Boolean = true,
                             throttleRate: Double = 1024)

