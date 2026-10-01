package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.{SimulationParameters, SimulationState}
import io.codeswarm.schauberger.physics.{CompositeFlowForce, FlowContext}

/** Geometry-agnostic pure coordinator for fixed-step particle updates. */
final class SimulationEngine(forceModel: CompositeFlowForce, integrator: ParticleIntegrator, particleGenerator: ParticleGenerator, boundaryHandler: BoundaryHandler) {
  def initialState(parameters: SimulationParameters, geometry: PipeGeometry): SimulationState = SimulationState(particleGenerator.generate(parameters.particleCount, geometry, parameters), 0.0, 0L)
  def step(state: SimulationState, parameters: SimulationParameters, geometry: PipeGeometry, deltaTime: Double): SimulationState = {
    require(deltaTime >= 0.0)
    val context = FlowContext(geometry, parameters)
    val next = state.particles.map { p =>
      val a = forceModel.acceleration(p, context)
      boundaryHandler.handle(integrator.integrate(p, a, deltaTime, parameters.maxVelocity), geometry, parameters, particleGenerator)
    }
    SimulationState(next, state.elapsedTime + deltaTime, state.frame + 1L)
  }
}
