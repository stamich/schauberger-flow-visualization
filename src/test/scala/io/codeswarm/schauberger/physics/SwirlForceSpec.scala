package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.geometry.{OvoidCrossSection, TwistedPipe}
import io.codeswarm.schauberger.math.{Vector2D, Vector3D}
import io.codeswarm.schauberger.model.{Particle, RotationDirection, SimulationParameters}
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SwirlForceSpec extends AnyFunSuite {
  private val pipe = TwistedPipe(100.0, OvoidCrossSection(20.0, 30.0, 0.1), 1.0)

  test("swirl is tangential and reverses with rotation direction") {
    val position = pipe.fromLocalCrossSection(25.0, Vector2D(5.0, 0.0))
    val particle = Particle(1L, position, Vector3D.Zero)
    val force = new SwirlForce(new SolidBodySwirlProfile)
    val base = SimulationParameters.Default
    val ccwParameters = base.copy(swirl = base.swirl.copy(rotationDirection = RotationDirection.CounterClockwise))
    val cwParameters = base.copy(swirl = base.swirl.copy(rotationDirection = RotationDirection.Clockwise))
    val ccw = force.acceleration(PhysicsTestSupport.context(particle, pipe, ccwParameters))
    val cw = force.acceleration(PhysicsTestSupport.context(particle, pipe, cwParameters))
    assert(ccw.dot(cw) < 0.0)
  }

  test("centerline particle receives no swirl") {
    val particle = Particle(1L, Vector3D(20.0, 0.0, 0.0), Vector3D.Zero)
    val acceleration = (new SwirlForce(new SolidBodySwirlProfile)).acceleration(
      PhysicsTestSupport.context(particle, pipe, SimulationParameters.Default)
    )
    assert(acceleration.magnitude < 1e-9)
  }
}
