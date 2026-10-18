package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.geometry.{CircularCrossSection, StraightPipe, TwistedPipe, OvoidCrossSection}
import io.codeswarm.schauberger.math.{Vector2D, Vector3D}
import io.codeswarm.schauberger.model.{Particle, RadialBalanceParameters, SimulationParameters}
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CentripetalBalanceForceSpec extends AnyFunSuite {
  private val pipe = StraightPipe(100.0, CircularCrossSection(10.0))
  private val force = new CentripetalBalanceForce

  private def acceleration(position: Vector3D, velocity: Vector3D, parameters: SimulationParameters = SimulationParameters.Default): Vector3D =
    force.acceleration(PhysicsTestSupport.context(Particle(1L, position, velocity), pipe, parameters))

  test("a tracer with tangential speed v at radius r is pulled to the center with v squared over r") {
    // Radius 5 along +y, tangential speed 10 along +z: expected inward acceleration 100 / 5 = 20 along -y.
    val result = acceleration(Vector3D(20.0, 5.0, 0.0), Vector3D(0.0, 0.0, 10.0))
    assert(math.abs(result.x) < 1e-12)
    assert(math.abs(result.y + 20.0) < 1e-9)
    assert(math.abs(result.z) < 1e-9)
  }

  test("the balance does not depend on the rotation direction") {
    val ccw = acceleration(Vector3D(20.0, 5.0, 0.0), Vector3D(0.0, 0.0, 10.0))
    val cw = acceleration(Vector3D(20.0, 5.0, 0.0), Vector3D(0.0, 0.0, -10.0))
    assert((ccw - cw).magnitude < 1e-9)
  }

  test("purely radial and axial motion is not balanced") {
    assert(acceleration(Vector3D(20.0, 5.0, 0.0), Vector3D(90.0, 12.0, 0.0)).magnitude < 1e-12)
  }

  test("strength scales the acceleration linearly and zero strength or disabled balance removes it") {
    val base = SimulationParameters.Default
    val position = Vector3D(20.0, 5.0, 0.0)
    val velocity = Vector3D(0.0, 0.0, 10.0)
    val full = acceleration(position, velocity, base.copy(radialBalance = RadialBalanceParameters(enabled = true, strength = 1.0)))
    val half = acceleration(position, velocity, base.copy(radialBalance = RadialBalanceParameters(enabled = true, strength = 0.5)))
    assert((full * 0.5 - half).magnitude < 1e-9)
    assert(acceleration(position, velocity, base.copy(radialBalance = RadialBalanceParameters(enabled = true, strength = 0.0))) == Vector3D.Zero)
    assert(acceleration(position, velocity, base.copy(radialBalance = RadialBalanceParameters(enabled = false, strength = 1.0))) == Vector3D.Zero)
  }

  test("a particle on the axis receives no balance acceleration") {
    assert(acceleration(Vector3D(20.0, 0.0, 0.0), Vector3D(0.0, 0.0, 50.0)) == Vector3D.Zero)
  }

  test("acceleration stays bounded for fast tracers close to the axis") {
    val speed = 80.0
    val result = acceleration(Vector3D(20.0, 1e-6, 0.0), Vector3D(0.0, 0.0, speed))
    val bound = speed * speed / (CentripetalBalanceForce.RadiusFloorRatio * pipe.characteristicRadius)
    assert(result.magnitude <= bound + 1e-9)
    assert(!result.magnitude.isNaN)
  }

  test("the balance works in the rotated frame of a twisted ovoid") {
    val twisted = TwistedPipe(100.0, OvoidCrossSection(20.0, 30.0, 0.1), 1.0)
    val position = twisted.fromLocalCrossSection(40.0, Vector2D(4.0, 3.0))
    val frame = twisted.localFrameAt(position)
    val radial = frame.crossSectionVectorToWorld(Vector2D(4.0, 3.0))
    val tangentialUnit = frame.tangent.cross(radial.normalized)
    val particle = Particle(1L, position, tangentialUnit * 8.0)
    val result = force.acceleration(PhysicsTestSupport.context(particle, twisted, SimulationParameters.Default))
    val expected = radial.normalized * (-64.0 / radial.magnitude)
    assert((result - expected).magnitude < 1e-9)
    assert(math.abs(result.dot(frame.tangent)) < 1e-9)
  }
}
