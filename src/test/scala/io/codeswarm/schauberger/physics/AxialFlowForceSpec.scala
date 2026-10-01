package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.geometry.StraightCircularPipe
import io.codeswarm.schauberger.math.Vector2D
import io.codeswarm.schauberger.model.{Particle, SimulationParameters}
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

/** Unit tests for target-velocity axial flow. */
@RunWith(classOf[JUnitRunner])
final class AxialFlowForceSpec extends AnyFunSuite {
  private val force = new AxialFlowForce
  private val params = SimulationParameters.Default.copy(axialVelocity = 100.0, axialResponse = 2.0)
  private val context = FlowContext(StraightCircularPipe(1000.0, 100.0), params)

  test("particle slower than target receives positive x acceleration") {
    val a = force.acceleration(Particle(1L, Vector2D.Zero, Vector2D(80.0, 4.0)), context)
    assert(a == Vector2D(40.0, 0.0))
  }

  test("particle at target velocity receives zero axial acceleration") {
    val a = force.acceleration(Particle(1L, Vector2D.Zero, Vector2D(100.0, 4.0)), context)
    assert(a == Vector2D.Zero)
  }

  test("particle faster than target receives negative x acceleration") {
    val a = force.acceleration(Particle(1L, Vector2D.Zero, Vector2D(120.0, 0.0)), context)
    assert(a == Vector2D(-40.0, 0.0))
  }
}
