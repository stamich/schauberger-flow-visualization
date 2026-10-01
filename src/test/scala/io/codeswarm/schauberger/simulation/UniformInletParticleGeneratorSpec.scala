package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.StraightCircularPipe
import io.codeswarm.schauberger.model.SimulationParameters
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

/** Tests deterministic and valid particle generation. */
@RunWith(classOf[JUnitRunner])
final class UniformInletParticleGeneratorSpec extends AnyFunSuite {
  private val pipe = StraightCircularPipe(1000.0, 100.0)
  private val params = SimulationParameters.Default.copy(particleCount = 100)

  test("generator returns requested count with unique stable ids") {
    val particles = new UniformInletParticleGenerator(42L).generate(100, pipe, params)
    assert(particles.size == 100)
    assert(particles.map(_.id).distinct.size == 100)
  }

  test("all generated particles begin inside the pipe") {
    val particles = new UniformInletParticleGenerator(42L).generate(100, pipe, params)
    assert(particles.forall(p => pipe.contains(p.position)))
  }

  test("same seed produces identical population") {
    val a = new UniformInletParticleGenerator(42L).generate(20, pipe, params)
    val b = new UniformInletParticleGenerator(42L).generate(20, pipe, params)
    assert(a == b)
  }
}
