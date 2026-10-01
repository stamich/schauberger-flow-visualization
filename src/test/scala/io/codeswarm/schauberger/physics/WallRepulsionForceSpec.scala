package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.geometry.StraightCircularPipe
import io.codeswarm.schauberger.math.Vector2D
import io.codeswarm.schauberger.model.{Particle, SimulationParameters}
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

/** Unit tests for the soft wall-repulsion model. */
@RunWith(classOf[JUnitRunner])
final class WallRepulsionForceSpec extends AnyFunSuite {
  private val force = new WallRepulsionForce
  private val params = SimulationParameters.Default.copy(wallThreshold = 10.0, wallStrength = 100.0)
  private val context = FlowContext(StraightCircularPipe(100.0, 50.0), params)

  test("center particle receives no wall force") {
    assert(force.acceleration(Particle(1L, Vector2D(20.0, 0.0), Vector2D.Zero), context) == Vector2D.Zero)
  }

  test("particle near upper wall is accelerated downward") {
    val result = force.acceleration(Particle(1L, Vector2D(20.0, 45.0), Vector2D.Zero), context)
    assert(result.y < 0.0)
    assert(result.x == 0.0)
  }

  test("particle near lower wall is accelerated upward") {
    val result = force.acceleration(Particle(1L, Vector2D(20.0, -45.0), Vector2D.Zero), context)
    assert(result.y > 0.0)
    assert(result.x == 0.0)
  }
}
