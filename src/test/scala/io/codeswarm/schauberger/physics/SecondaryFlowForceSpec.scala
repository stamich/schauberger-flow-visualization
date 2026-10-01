package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.geometry.{OvoidCrossSection, TwistedPipe}
import io.codeswarm.schauberger.math.{Vector2D, Vector3D}
import io.codeswarm.schauberger.model.{Particle, SimulationParameters}
import io.codeswarm.schauberger.physics.secondary.TwinVortexSecondaryFlow
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SecondaryFlowForceSpec extends AnyFunSuite {
  private val geometry = TwistedPipe(100.0, OvoidCrossSection(20.0, 30.0, 0.1), 1.0)
  private val position = geometry.fromLocalCrossSection(50.0, Vector2D(4.0, 3.0))
  private val particle = Particle(1L, position, Vector3D.Zero)
  private val force = new SecondaryFlowForce(new TwinVortexSecondaryFlow)

  test("enabled secondary model contributes cross-sectional acceleration") {
    val acceleration = force.acceleration(particle, FlowContext(geometry, SimulationParameters.Default))
    assert(acceleration.magnitude > 0.0)
    assert(math.abs(acceleration.dot(geometry.tangentAt(position))) < 1e-8)
  }

  test("disabled secondary flow contributes zero acceleration") {
    val base = SimulationParameters.Default
    val parameters = base.copy(secondaryFlow = base.secondaryFlow.copy(enabled = false))
    val acceleration = force.acceleration(particle, FlowContext(geometry, parameters))
    assert(acceleration == Vector3D.Zero)
  }
}
