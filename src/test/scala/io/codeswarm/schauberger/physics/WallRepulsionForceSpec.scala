package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.geometry.{OvoidCrossSection, StraightPipe}
import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.{Particle, SimulationParameters}
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class WallRepulsionForceSpec extends AnyFunSuite {
  test("wall force uses cached geometry normal") {
    val pipe = StraightPipe(100.0, OvoidCrossSection(20.0, 30.0, 0.2))
    val particle = Particle(1L, Vector3D(20.0, 9.8, 0.0), Vector3D.Zero)
    val base = SimulationParameters.Default
    val parameters = base.copy(wall = base.wall.copy(threshold = 3.0))
    val context = PhysicsTestSupport.context(particle, pipe, parameters)
    val acceleration = (new WallRepulsionForce).acceleration(context)
    assert(acceleration.dot(context.geometry.inwardNormal) >= 0.0)
  }
}
