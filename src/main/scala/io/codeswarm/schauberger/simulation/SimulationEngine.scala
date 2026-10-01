package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.{SimulationParameters, SimulationState}
import io.codeswarm.schauberger.physics.{CompositeFlowForce, FlowContext}

/** Pure simulation coordinator for one fixed physics step.
  *
  * The engine depends only on domain abstractions and contains no ScalaFX code.
  */
final class SimulationEngine(
    geometry: PipeGeometry,
    forceModel: CompositeFlowForce,
    integrator: ParticleIntegrator,
    particleGenerator: ParticleGenerator,
    boundaryHandler: BoundaryHandler
) {

  /** Creates a fresh deterministic state using the supplied parameters. */
  def initialState(parameters: SimulationParameters): SimulationState =
    SimulationState(
      particles = particleGenerator.generate(parameters.particleCount, geometry, parameters),
      elapsedTime = 0.0,
      frame = 0L
    )

  /** Advances all particles by one physics step and returns a new immutable state. */
  def step(
      state: SimulationState,
      parameters: SimulationParameters,
      deltaTime: Double
  ): SimulationState = {
    require(deltaTime >= 0.0, "deltaTime must be non-negative")
    val context = FlowContext(geometry, parameters)
    val particles = state.particles.map { particle =>
      val acceleration = forceModel.acceleration(particle, context)
      val integrated = integrator.integrate(particle, acceleration, deltaTime, parameters.maxVelocity)
      boundaryHandler.handle(integrated, geometry, parameters, particleGenerator)
    }
    SimulationState(
      particles = particles,
      elapsedTime = state.elapsedTime + deltaTime,
      frame = state.frame + 1L
    )
  }
}
