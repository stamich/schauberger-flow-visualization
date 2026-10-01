package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.geometry.StraightCircularPipe
import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.{Particle, SimulationParameters}
import org.scalatest.funsuite.AnyFunSuite

/** Tests radial soft-wall confinement in three dimensions. */
final class WallRepulsionForceSpec extends AnyFunSuite {
  private val force = new WallRepulsionForce
  private val context = FlowContext(
    StraightCircularPipe(100, 10),
    SimulationParameters.Default.copy(wallThreshold = 2, wallStrength = 100)
  )

  test("center-line particle receives no wall acceleration") {
    assert(force.acceleration(Particle(1, Vector3D(10, 0, 0), Vector3D.Zero), context) == Vector3D.Zero)
  }

  test("particle near positive y wall is pushed inward") {
    val a = force.acceleration(Particle(1, Vector3D(10, 9.5, 0), Vector3D.Zero), context)
    assert(a.y < 0.0)
    assert(math.abs(a.z) < 1e-12)
  }

  test("particle near negative z wall is pushed toward positive z") {
    val a = force.acceleration(Particle(1, Vector3D(10, 0, -9.5), Vector3D.Zero), context)
    assert(a.z > 0.0)
  }
}
