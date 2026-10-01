package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.{Particle, SimulationParameters}

/** Strategy enforcing hard domain boundaries after numerical integration. */
trait BoundaryHandler {
  /** Applies axial respawn and radial confinement to one particle. */
  def handle(
      particle: Particle,
      geometry: PipeGeometry,
      parameters: SimulationParameters,
      generator: ParticleGenerator
  ): Particle
}
