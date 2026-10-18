package io.codeswarm.schauberger.diagnostics

import io.codeswarm.schauberger.model.{CounterRotatingParameters, RankineParameters, SimulationParameters}
import io.codeswarm.schauberger.physics.swirl.SwirlProfileFactory
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RadialProfileSamplerSpec extends AnyFunSuite {
  test("sampler covers zero through unit normalized radius and reports peak") {
    val sampler = new RadialProfileSampler(new SwirlProfileFactory)
    val parameters = SimulationParameters.Default.swirl.copy(profile = RankineParameters(0.35))
    val diagnostics = sampler.sample(parameters, 100.0, 101)
    assert(diagnostics.samples.size == 101)
    assert(diagnostics.samples.head.normalizedRadius == 0.0)
    assert(diagnostics.samples.last.normalizedRadius == 1.0)
    assert(diagnostics.peakVelocity > 0.0)
    assert(math.abs(diagnostics.peakRadius - 0.35) <= 0.02)
  }

  test("simple profiles have no reversed region and no zero crossing") {
    val sampler = new RadialProfileSampler(new SwirlProfileFactory)
    val diagnostics = sampler.sample(SimulationParameters.Default.swirl.copy(profile = RankineParameters(0.35)), 100.0, 201)
    assert(diagnostics.zeroCrossings.isEmpty)
    assert(diagnostics.reverseVelocity == 0.0)
    assert(!diagnostics.isCounterRotating)
  }

  test("counter-rotating profile reports its reversed region and one zero crossing") {
    val sampler = new RadialProfileSampler(new SwirlProfileFactory)
    val swirl = SimulationParameters.Default.swirl.copy(profile = CounterRotatingParameters())
    val diagnostics = sampler.sample(swirl, 100.0, 401)
    assert(diagnostics.isCounterRotating)
    assert(diagnostics.zeroCrossings.size == 1)
    assert(diagnostics.reverseVelocity < 0.0)
    assert(diagnostics.samples.head.tangentialVelocity == 0.0)
    assert(diagnostics.samples.last.tangentialVelocity < 0.0)
  }

  test("peak is the sample of largest magnitude even when it is negative") {
    val sampler = new RadialProfileSampler(new SwirlProfileFactory)
    val strongOuter = CounterRotatingParameters(innerStrength = 0.2, outerStrength = 1.5)
    val diagnostics = sampler.sample(SimulationParameters.Default.swirl.copy(profile = strongOuter), 100.0, 401)
    assert(diagnostics.peakVelocity < 0.0)
    assert(diagnostics.peakVelocity == diagnostics.reverseVelocity)
  }
}
