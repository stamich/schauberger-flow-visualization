package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.StraightCircularPipe
import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.{Particle, SimulationParameters, SimulationState}
import io.codeswarm.schauberger.physics.{AxialFlowForce, CompositeFlowForce, SolidBodySwirlProfile, SwirlForce, WallRepulsionForce}
import org.scalatest.funsuite.AnyFunSuite

/** Integration tests for the complete milestone 0.2 physics pipeline. */
final class SimulationEngineSpec extends AnyFunSuite {
  private val pipe = StraightCircularPipe(1000, 120)
  private val engine = new SimulationEngine(
    pipe,
    CompositeFlowForce(Vector(new AxialFlowForce, new SwirlForce(new SolidBodySwirlProfile), new WallRepulsionForce)),
    new SemiImplicitEulerIntegrator,
    new UniformInletParticleGenerator(42),
    new PipeBoundaryHandler
  )

  test("particle count remains constant and all particles remain inside the pipe") {
    val params = SimulationParameters.Default.copy(particleCount = 200)
    var state = engine.initialState(params)
    var i = 0
    while (i < 3000) {
      state = engine.step(state, params, params.fixedTimeStep)
      i += 1
    }
    assert(state.particles.size == 200)
    assert(state.particles.forall(p => pipe.contains(p.position)))
    assert(state.frame == 3000)
    assert(state.elapsedTime > 0.0)
  }

  test("swirl creates tangential motion away from the center line") {
    val params = SimulationParameters.Default.copy(particleCount = 100, angularVelocity = 1.2, swirlResponse = 4.0)
    var state = engine.initialState(params)
    var i = 0
    while (i < 240) {
      state = engine.step(state, params, params.fixedTimeStep)
      i += 1
    }
    val meanCrossSectionSpeed = state.particles.map(p => math.hypot(p.velocity.y, p.velocity.z)).sum / state.particles.size
    assert(meanCrossSectionSpeed > 0.1)
  }

  test("helical motion increases x while changing cross-section angle") {
    val params = SimulationParameters.Default.copy(particleCount = 1, angularVelocity = 1.5, swirlResponse = 5.0)
    val p0 = Particle(0L, Vector3D(10.0, 50.0, 0.0), Vector3D(params.axialVelocity, 0.0, 0.0))
    var state = SimulationState(Vector(p0), 0.0, 0L)
    var i = 0
    while (i < 120) {
      state = engine.step(state, params, params.fixedTimeStep)
      i += 1
    }
    val p1 = state.particles.head
    val a0 = math.atan2(p0.position.z, p0.position.y)
    val a1 = math.atan2(p1.position.z, p1.position.y)
    assert(p1.position.x > p0.position.x)
    assert(math.abs(a1 - a0) > 1e-3)
  }
}
