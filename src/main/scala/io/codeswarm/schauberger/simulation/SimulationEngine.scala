package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.{SimulationParameters, SimulationState}
import io.codeswarm.schauberger.physics.{CompositeFlowForce, FlowContext}

/** Pure simulation coordinator for one fixed physics step.
  *
  * The engine knows only domain abstractions and has no ScalaFX dependency.
  *
  * @param geometry pipe geometry
  * @param forceModel combined acceleration model
  * @param integrator numerical particle integrator
  * @param particleGenerator initial-state generator
  * @param boundaryHandler domain-boundary policy
  */
final class SimulationEngine(
    geometry: PipeGeometry,
    forceModel: CompositeFlowForce,
    integrator: ParticleIntegrator,
    particleGenerator: ParticleGenerator,
    boundaryHandler: BoundaryHandler
) {

  /** Creates a fresh deterministic state from current parameters. */
  def initialState(parameters: SimulationParameters): SimulationState =
    SimulationState(
      particles = particleGenerator.generate(parameters.particleCount, geometry, parameters),
      elapsedTime = 0.0,
      frame = 0L
    )

  /** Advances the complete simulation by `deltaTime` seconds. */
  def step(
      state: SimulationState,
      parameters: SimulationParameters,
      deltaTime: Double
  ): SimulationState = {
    require(deltaTime > 0.0, "deltaTime must be positive")
    val context = FlowContext(geometry, parameters)
    val nextParticles = state.particles.map { particle =>
      val acceleration = forceModel.acceleration(particle, context)
      val integrated = integrator.integrate(particle, acceleration, deltaTime)
      val limited = integrated.copy(velocity = integrated.velocity.limit(parameters.maxVelocity))
      boundaryHandler.handle(limited, geometry, parameters)
    }

    state.copy(
      particles = nextParticles,
      elapsedTime = state.elapsedTime + deltaTime,
      frame = state.frame + 1L
    )
  }
}
