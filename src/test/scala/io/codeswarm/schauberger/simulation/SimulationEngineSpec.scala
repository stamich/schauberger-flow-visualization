package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.StraightCircularPipe
import io.codeswarm.schauberger.model.SimulationParameters
import io.codeswarm.schauberger.physics.{AxialFlowForce, CompositeFlowForce, WallRepulsionForce}
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

/** Integration tests for the headless simulation kernel. */
@RunWith(classOf[JUnitRunner])
final class SimulationEngineSpec extends AnyFunSuite {
  private val pipe = StraightCircularPipe(1000.0, 100.0)
  private val params = SimulationParameters.Default.copy(particleCount = 250, axialVelocity = 100.0)
  private val engine = new SimulationEngine(
    pipe,
    CompositeFlowForce(Vector(new AxialFlowForce, new WallRepulsionForce)),
    new SemiImplicitEulerIntegrator,
    new UniformInletParticleGenerator(42L),
    new PipeBoundaryHandler
  )

  test("initial state has requested count and zero clock") {
    val state = engine.initialState(params)
    assert(state.particles.size == params.particleCount)
    assert(state.elapsedTime == 0.0)
    assert(state.frame == 0L)
  }

  test("many updates keep particle count constant and particles inside domain") {
    val initial = engine.initialState(params)
    val result = (1 to 3000).foldLeft(initial) { (state, _) =>
      engine.step(state, params, params.fixedTimeStep)
    }

    assert(result.particles.size == params.particleCount)
    assert(result.particles.forall(p => pipe.contains(p.position)))
    assert(result.frame == 3000L)
    assert(math.abs(result.elapsedTime - 3000.0 * params.fixedTimeStep) < 1e-9)
  }

  test("average axial velocity approaches configured target") {
    val initial = engine.initialState(params)
    val result = (1 to 600).foldLeft(initial) { (state, _) =>
      engine.step(state, params, params.fixedTimeStep)
    }
    val averageVx = result.particles.map(_.velocity.x).sum / result.particles.size
    assert(math.abs(averageVx - params.axialVelocity) < 4.0)
  }
}
