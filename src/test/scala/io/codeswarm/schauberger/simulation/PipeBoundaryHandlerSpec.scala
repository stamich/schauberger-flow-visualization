package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.StraightCircularPipe
import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.{Particle, SimulationParameters}
import org.scalatest.funsuite.AnyFunSuite

/** Tests hard outlet and cylindrical wall handling. */
final class PipeBoundaryHandlerSpec extends AnyFunSuite {
  private val pipe = StraightCircularPipe(100, 10)
  private val params = SimulationParameters.Default
  private val generator = new UniformInletParticleGenerator(42)
  private val handler = new PipeBoundaryHandler

  test("particle past outlet respawns at inlet") {
    val p = handler.handle(Particle(7, Vector3D(101, 0, 0), Vector3D(10, 0, 0)), pipe, params, generator)
    assert(p.id == 7)
    assert(p.position.x == 0.0)
    assert(pipe.radialDistance(p.position) <= pipe.radius)
  }

  test("outside radial point is clamped and outward normal velocity removed") {
    val p = handler.handle(Particle(7, Vector3D(50, 12, 0), Vector3D(10, 5, 0)), pipe, params, generator)
    assert(pipe.radialDistance(p.position) < pipe.radius)
    assert(p.velocity.y <= 1e-12)
  }
}
