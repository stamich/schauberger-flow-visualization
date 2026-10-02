package io.codeswarm.schauberger.diagnostics

import io.codeswarm.schauberger.model.{RankineParameters, SimulationParameters}
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
}
