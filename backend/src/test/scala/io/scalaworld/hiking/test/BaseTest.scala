package io.scalaworld.hiking.test

import io.scalaworld.hiking.infrastructure.CorrelationId
import org.scalatest.{FlatSpec, Matchers}

trait BaseTest extends FlatSpec with Matchers {
  CorrelationId.init()
  val testClock = new TestClock()
}
