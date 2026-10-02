package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.{Particle, SimulationParameters}

/** Enforces hard domain boundaries after numerical integration. */
trait BoundaryHandler {
  def handle(particle: Particle, geometry: PipeGeometry, parameters: SimulationParameters, generator: ParticleGenerator): Particle
}
