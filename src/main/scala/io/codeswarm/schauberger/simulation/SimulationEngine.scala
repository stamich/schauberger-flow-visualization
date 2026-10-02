package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.{SimulationParameters, SimulationState}
import io.codeswarm.schauberger.physics.{CompositeFlowForce, FlowContext}

/** Geometry-agnostic pure coordinator for fixed-step particle updates. */
final class SimulationEngine(
                              forceModel: CompositeFlowForce,
                              integrator: ParticleIntegrator,
                              particleGenerator: ParticleGenerator,
                              boundaryHandler: BoundaryHandler,
                              geometryContextCalculator: GeometryContextCalculator = new DefaultGeometryContextCalculator
                            ) {
  /** Creates deterministic initial particle state for one geometry. */
  def initialState(parameters: SimulationParameters, geometry: PipeGeometry): SimulationState =
    SimulationState(particleGenerator.generate(parameters.particleCount, geometry, parameters), 0.0, 0L)

  /** Advances all particles by one fixed simulation step. */
  def step(state: SimulationState, parameters: SimulationParameters, geometry: PipeGeometry, deltaTime: Double): SimulationState = {
    require(deltaTime >= 0.0)
    val flowContext = FlowContext(geometry, parameters)
    val next = state.particles.map { particle =>
      val geometryContext = geometryContextCalculator.calculate(particle, geometry)
      val context = ParticleFlowContext(particle, flowContext, geometryContext)
      val acceleration = forceModel.acceleration(context)
      val integrated = integrator.integrate(particle, acceleration, deltaTime, parameters.maxVelocity)
      boundaryHandler.handle(integrated, geometry, parameters, particleGenerator)
    }
    SimulationState(next, state.elapsedTime + deltaTime, state.frame + 1L)
  }
}
