package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.geometry.{OvoidCrossSection, TwistedPipe}
import io.codeswarm.schauberger.math.{Vector2D, Vector3D}
import io.codeswarm.schauberger.model.{Particle, RankineParameters, RotationDirection, SimulationParameters}
import io.codeswarm.schauberger.physics.secondary.TwinVortexSecondaryFlow
import io.codeswarm.schauberger.physics.swirl.SwirlProfileFactory
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CrossSectionFlowForceSpec extends AnyFunSuite {
  private val pipe = TwistedPipe(100.0, OvoidCrossSection(20.0, 30.0, 0.1), 1.0)
  private val force = new CrossSectionFlowForce(new SwirlProfileFactory, new TwinVortexSecondaryFlow)

  private val Default = SimulationParameters.Default
  private val NoSecondary = Default.copy(secondaryFlow = Default.secondaryFlow.copy(enabled = false))
  private val NoSwirl = Default.copy(swirl = Default.swirl.copy(angularVelocity = 0.0))

  private def at(axial: Double, x: Double, y: Double, velocity: Vector3D = Vector3D.Zero): Particle =
    Particle(1L, pipe.fromLocalCrossSection(axial, Vector2D(x, y)), velocity)

  private def accelerationOf(particle: Particle, parameters: SimulationParameters): Vector3D =
    force.acceleration(PhysicsTestSupport.context(particle, pipe, parameters))

  test("swirl is tangential and reverses with rotation direction") {
    val particle = at(25.0, 5.0, 0.0)
    val ccwParameters = NoSecondary.copy(swirl = NoSecondary.swirl.copy(rotationDirection = RotationDirection.CounterClockwise))
    val cwParameters = NoSecondary.copy(swirl = NoSecondary.swirl.copy(rotationDirection = RotationDirection.Clockwise))
    val ccw = accelerationOf(particle, ccwParameters)
    val cw = accelerationOf(particle, cwParameters)
    assert(ccw.dot(cw) < 0.0)
  }

  test("centerline particle receives no swirl") {
    val particle = Particle(1L, Vector3D(20.0, 0.0, 0.0), Vector3D.Zero)
    val acceleration = accelerationOf(particle, NoSecondary)
    assert(acceleration.magnitude < 1e-9)
  }

  test("selected vortex profile changes target acceleration") {
    val particle = at(25.0, 8.0, 0.0)
    val rankine = NoSecondary.copy(swirl = NoSecondary.swirl.copy(profile = RankineParameters(0.25)))
    val a = accelerationOf(particle, NoSecondary)
    val b = accelerationOf(particle, rankine)
    assert(math.abs(a.magnitude - b.magnitude) > 1e-9)
  }

  test("secondary model contributes cross-sectional acceleration without swirl") {
    val particle = at(50.0, 4.0, 3.0)
    val context = PhysicsTestSupport.context(particle, pipe, NoSwirl)
    val acceleration = force.acceleration(context)
    assert(acceleration.magnitude > 0.0)
    assert(math.abs(acceleration.dot(context.geometry.frame.tangent)) < 1e-8)
  }

  test("disabled secondary flow and disabled swirl give zero acceleration") {
    val parameters = NoSwirl.copy(secondaryFlow = NoSwirl.secondaryFlow.copy(enabled = false))
    assert(accelerationOf(at(50.0, 4.0, 3.0), parameters) == Vector3D.Zero)
  }

  test("result never has an axial component") {
    val particle = at(50.0, 4.0, 3.0, Vector3D(60.0, 1.0, -2.0))
    val context = PhysicsTestSupport.context(particle, pipe, Default)
    val acceleration = force.acceleration(context)
    assert(math.abs(acceleration.dot(context.geometry.frame.tangent)) < 1e-8)
  }

  test("a particle already at the combined target is in equilibrium") {
    // Build the target from the force itself: relax once from rest, then place the
    // particle at that in-plane velocity scaled so the relaxation is exactly zero
    // along each axis. For a single-axis check, use swirl only.
    val particle0 = at(25.0, 6.0, 0.0)
    val context0 = PhysicsTestSupport.context(particle0, pipe, NoSecondary)
    val response = NoSecondary.swirl.response
    val target = force.acceleration(context0) / response // velocity at which the relaxation vanishes
    val settled = particle0.copy(velocity = target)
    val acceleration = force.acceleration(PhysicsTestSupport.context(settled, pipe, NoSecondary))
    assert(acceleration.magnitude < 1e-9)
  }

  test("swirl reaches its full profile target when secondary flow is enabled") {
    // The secondary target is added to the swirl target instead of competing with it, so
    // the stationary tangential speed is the profile speed plus the secondary component.
    val particle = at(50.0, 6.0, 0.0)
    val context = PhysicsTestSupport.context(particle, pipe, Default)
    val radial = context.geometry.frame.crossSectionVectorToWorld(context.geometry.localPosition)
    val tangentialUnit = context.geometry.frame.tangent.normalized.cross(radial).normalized
    val withSecondary = force.acceleration(context).dot(tangentialUnit) / Default.swirl.response
    val withoutSecondary = accelerationOf(particle, NoSecondary).dot(tangentialUnit) / Default.swirl.response
    val secondaryTarget = context.geometry.frame.crossSectionVectorToWorld(
      new TwinVortexSecondaryFlow().targetVelocity(context.geometry.localPosition, pipe, particle.position.x, Default.secondaryFlow)
    )
    assert(math.abs((withSecondary - withoutSecondary) - secondaryTarget.dot(tangentialUnit)) < 1e-6)
  }

  test("secondary component is independent of rotation direction") {
    val particle = at(50.0, 4.0, 3.0)
    val base = NoSwirl
    val cw = base.copy(swirl = base.swirl.copy(rotationDirection = RotationDirection.Clockwise))
    val ccw = base.copy(swirl = base.swirl.copy(rotationDirection = RotationDirection.CounterClockwise))
    val a = accelerationOf(particle, cw)
    val b = accelerationOf(particle, ccw)
    assert((a - b).magnitude < 1e-12)
  }
}
