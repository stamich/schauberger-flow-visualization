package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.StraightCircularPipe
import io.codeswarm.schauberger.model.SimulationParameters
import org.scalatest.funsuite.AnyFunSuite

/** Tests deterministic 3D inlet population generation. */
final class UniformInletParticleGeneratorSpec extends AnyFunSuite {
  test("same seed creates the same initial population") {
    val pipe = StraightCircularPipe(100, 10)
    val params = SimulationParameters.Default.copy(particleCount = 20)
    val a = new UniformInletParticleGenerator(42).generate(20, pipe, params)
    val b = new UniformInletParticleGenerator(42).generate(20, pipe, params)
    assert(a == b)
    assert(a.forall(p => pipe.contains(p.position)))
  }
}
