package io.codeswarm.schauberger.model

import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class VortexRegionSpec extends AnyFunSuite {
  test("positive, negative and negligible angular velocities are told apart") {
    assert(VortexRegion.classify(0.4, 0.5) == VortexRegion.Forward)
    assert(VortexRegion.classify(-0.4, 0.5) == VortexRegion.Reverse)
    assert(VortexRegion.classify(0.01, 0.5) == VortexRegion.Quiescent)
    assert(VortexRegion.classify(-0.01, 0.5) == VortexRegion.Quiescent)
  }

  test("nothing swirls when the configured angular velocity is not positive") {
    assert(VortexRegion.classify(0.4, 0.0) == VortexRegion.Quiescent)
  }

  test("the dead band edge counts as quiescent") {
    assert(VortexRegion.classify(VortexRegion.DeadBand * 0.5, 0.5) == VortexRegion.Quiescent)
  }

  test("metrics from accumulators keep the senses apart") {
    val metrics = VortexRegionMetrics.fromAccumulators(3L, 1L, 0L, 1.5, -0.5, 2.0)
    assert(metrics.forwardShare == 0.75 && metrics.reverseShare == 0.25 && metrics.quiescentShare == 0.0)
    assert(metrics.meanForwardAngular == 0.5 && metrics.meanReverseAngular == -0.5)
    assert(metrics.meanAbsoluteAngular == 0.5)
    assert(metrics.isCounterRotating())
  }

  test("an empty or one-sided population is not counter-rotating") {
    assert(VortexRegionMetrics.fromAccumulators(0L, 0L, 0L, 0.0, 0.0, 0.0) == VortexRegionMetrics.Empty)
    assert(!VortexRegionMetrics.fromAccumulators(5L, 0L, 0L, 2.5, 0.0, 2.5).isCounterRotating())
  }
}
