package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.geometry.StraightCircularPipe
import io.codeswarm.schauberger.math.Vector2D
import io.codeswarm.schauberger.model.{Particle, SimulationParameters}
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

/** Verifies that force composition is additive and engine-independent. */
@RunWith(classOf[JUnitRunner])
final class CompositeFlowForceSpec extends AnyFunSuite {
  test("composite force sums individual acceleration vectors") {
    val first = new FlowForce {
      override def acceleration(particle: Particle, context: FlowContext): Vector2D = Vector2D(1.0, 2.0)
    }
    val second = new FlowForce {
      override def acceleration(particle: Particle, context: FlowContext): Vector2D = Vector2D(3.0, -1.0)
    }
    val context = FlowContext(StraightCircularPipe(100.0, 10.0), SimulationParameters.Default)
    val total = CompositeFlowForce(Vector(first, second)).acceleration(Particle(1L, Vector2D.Zero, Vector2D.Zero), context)
    assert(total == Vector2D(4.0, 1.0))
  }
}
