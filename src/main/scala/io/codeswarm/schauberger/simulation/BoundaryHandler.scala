package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.{Particle, SimulationParameters}

/** Enforces the simulation-domain boundary semantics. */
trait BoundaryHandler {

  /** Corrects or respawns a particle after numerical integration. */
  def handle(particle: Particle, geometry: PipeGeometry, parameters: SimulationParameters): Particle
}
