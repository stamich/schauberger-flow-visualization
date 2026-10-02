package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.{OvoidCrossSection, TwistedPipe}
import io.codeswarm.schauberger.model.SimulationParameters
import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class UniformCrossSectionParticleGeneratorSpec extends AnyFunSuite {
  test("generator places all particles inside twisted ovoid") {
    val g = TwistedPipe(100, OvoidCrossSection(20, 30, 0.15), 1.2)
    val ps = new UniformCrossSectionParticleGenerator(42).generate(200, g, SimulationParameters.Default)
    assert(ps.forall(particle => g.contains(particle.position)))
  }

  test("same seed produces deterministic population") {
    val g = TwistedPipe(100, OvoidCrossSection(20, 30, 0.15), 1)
    assert(new UniformCrossSectionParticleGenerator(7).generate(20, g, SimulationParameters.Default) ==
      new UniformCrossSectionParticleGenerator(7).generate(20, g, SimulationParameters.Default))
  }
}
