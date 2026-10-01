package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.{Particle, SimulationParameters}

/** Strategy creating an initial deterministic tracer population. */
trait ParticleGenerator {
  /** Generates exactly count particles inside the supplied geometry. */
  def generate(
      count: Int,
      geometry: PipeGeometry,
      parameters: SimulationParameters
  ): Vector[Particle]

  /** Creates one inlet replacement for a particle that reached the outlet. */
  def respawn(
      particle: Particle,
      geometry: PipeGeometry,
      parameters: SimulationParameters
  ): Particle
}
