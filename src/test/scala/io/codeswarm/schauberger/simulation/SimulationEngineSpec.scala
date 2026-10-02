package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.{OvoidCrossSection, TwistedPipe}
import io.codeswarm.schauberger.model.SimulationParameters
import io.codeswarm.schauberger.physics.{AxialFlowForce, CompositeFlowForce, SecondaryFlowForce, SwirlForce, WallRepulsionForce}
import io.codeswarm.schauberger.physics.secondary.TwinVortexSecondaryFlow
import io.codeswarm.schauberger.physics.swirl.SwirlProfileFactory
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SimulationEngineSpec extends AnyFunSuite {
  private val secondaryModel = new TwinVortexSecondaryFlow
  private val engine = new SimulationEngine(
    CompositeFlowForce(Vector(
      new AxialFlowForce,
      new SwirlForce(new SwirlProfileFactory),
      new SecondaryFlowForce(secondaryModel),
      new WallRepulsionForce
    )),
    new SemiImplicitEulerIntegrator,
    new UniformCrossSectionParticleGenerator(42L),
    new PipeBoundaryHandler
  )

  test("long run preserves population, finite vectors and geometry invariant") {
    val geometry = TwistedPipe(300.0, OvoidCrossSection(80.0, 100.0, 0.14), 1.25)
    val parameters = SimulationParameters.Default.copy(particleCount = 120)
    var state = engine.initialState(parameters, geometry)
    var index = 0
    while (index < 2000) {
      state = engine.step(state, parameters, geometry, parameters.fixedTimeStep)
      index += 1
    }
    assert(state.particles.size == 120)
    assert(state.particles.forall(particle => geometry.contains(particle.position)))
    assert(state.particles.forall(particle => Seq(
      particle.position.x, particle.position.y, particle.position.z,
      particle.velocity.x, particle.velocity.y, particle.velocity.z
    ).forall(value => !value.isNaN && !value.isInfinity)))
  }

  test("secondary flow changes trajectory while preserving containment") {
    val geometry = TwistedPipe(300.0, OvoidCrossSection(80.0, 100.0, 0.10), 1.0)
    val enabled = SimulationParameters.Default.copy(particleCount = 1)
    val disabled = enabled.copy(secondaryFlow = enabled.secondaryFlow.copy(enabled = false))
    var enabledState = engine.initialState(enabled, geometry)
    var disabledState = engine.initialState(disabled, geometry)
    var index = 0
    while (index < 500) {
      enabledState = engine.step(enabledState, enabled, geometry, enabled.fixedTimeStep)
      disabledState = engine.step(disabledState, disabled, geometry, disabled.fixedTimeStep)
      index += 1
    }
    val enabledPosition = enabledState.particles.head.position
    val disabledPosition = disabledState.particles.head.position
    assert((enabledPosition - disabledPosition).magnitude > 1e-4)
    assert(geometry.contains(enabledPosition))
    assert(geometry.contains(disabledPosition))
  }
}
