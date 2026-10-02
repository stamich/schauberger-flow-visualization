package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.{Particle, SimulationParameters}
import io.codeswarm.schauberger.simulation.{DefaultGeometryContextCalculator, ParticleFlowContext}

/** Shared helpers for force tests using milestone 0.5 precomputed geometry contexts. */
object PhysicsTestSupport {

  /** Builds a complete per-particle flow context. */
  def context(particle: Particle, geometry: PipeGeometry, parameters: SimulationParameters): ParticleFlowContext = {
    val flow = FlowContext(geometry, parameters)
    val geometryContext = new DefaultGeometryContextCalculator().calculate(particle, geometry)
    ParticleFlowContext(particle, flow, geometryContext)
  }
}
