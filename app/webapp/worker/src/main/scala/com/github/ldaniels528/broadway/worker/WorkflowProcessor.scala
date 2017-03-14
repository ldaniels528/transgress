package com.github.ldaniels528.broadway.worker

import com.github.ldaniels528.broadway.models.Job
import com.github.ldaniels528.broadway.rest.LoggerFactory
import io.scalajs.JSON

/**
  * Workflow Processor
  * @author lawrence.daniels@gmail.com
  */
class WorkflowProcessor(workflow: Workflow, job: Job, statsGen: StatisticsGenerator) {
  private val logger = LoggerFactory.getLogger(getClass)

  def execute() = {
    logger.log(s"workflow = ${JSON.stringify(workflow)}")
  }

}
