package com.github.ldaniels528.transgress.worker

import scala.concurrent.Future

/**
  * Job Control Support
  * @author lawrence.daniels@gmail.com
  */
trait JobControlSupport {

  /**
    * Pauses the job
    * @return a completion promise
    */
  def pause(): Future[Boolean]

  /**
    * Resumes the paused or stopped the job
    * @return a completion promise
    */
  def resume(): Future[Boolean]

  /**
    * Stops the job
    * @return a completion promise
    */
  def stop(): Future[Boolean]

}
