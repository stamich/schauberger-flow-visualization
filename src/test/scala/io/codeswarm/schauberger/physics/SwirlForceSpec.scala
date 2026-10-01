package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.geometry.StraightCircularPipe
import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.{Particle, RotationDirection, SimulationParameters}
import org.scalatest.funsuite.AnyFunSuite

/** Unit tests for tangential acceleration around the pipe axis. */
final class SwirlForceSpec extends AnyFunSuite {
  private val force = new SwirlForce(new SolidBodySwirlProfile)
  private val pipe = StraightCircularPipe(100, 10)

  test("particle on positive y axis accelerates toward positive z for counter-clockwise rotation") {
    val params = SimulationParameters.Default.copy(
      angularVelocity = 1.0,
      swirlResponse = 1.0,
      rotationDirection = RotationDirection.CounterClockwise
    )
    val a = force.acceleration(Particle(1, Vector3D(10, 5, 0), Vector3D.Zero), FlowContext(pipe, params))
    assert(math.abs(a.y) < 1e-12)
    assert(a.z > 0.0)
  }

  test("clockwise rotation reverses tangential acceleration") {
    val params = SimulationParameters.Default.copy(
      angularVelocity = 1.0,
      swirlResponse = 1.0,
      rotationDirection = RotationDirection.Clockwise
    )
    val a = force.acceleration(Particle(1, Vector3D(10, 5, 0), Vector3D.Zero), FlowContext(pipe, params))
    assert(a.z < 0.0)
  }

  test("particle exactly on center line receives no swirl acceleration") {
    val a = force.acceleration(Particle(1, Vector3D(10, 0, 0), Vector3D.Zero), FlowContext(pipe, SimulationParameters.Default))
    assert(a == Vector3D.Zero)
  }
}
