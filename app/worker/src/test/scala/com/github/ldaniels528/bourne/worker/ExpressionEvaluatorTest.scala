package com.github.ldaniels528.transgress.worker

import org.scalatest.FunSpec

/**
  * Expression Evaluator Tests
  * @author lawrence.daniels@gmail.com
  */
class ExpressionEvaluatorTest extends FunSpec {

  describe("ExpressionEvaluator") {

    it("should replaces template variables") {
      val template = "./examples/incoming/LISTING_ACTIVITY-{{$date:YYYYMMDD}}.json"
      info(s"BEFORE: $template")
      val realized = ExpressionEvaluator.evaluate(template)
      info(s"AFTER: $realized")
    }

  }

}
