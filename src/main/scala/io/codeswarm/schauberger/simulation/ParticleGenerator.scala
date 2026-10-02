package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.{Particle, SimulationParameters}

/** Strategy creating and respawning tracer particles for arbitrary cross-sections. */
trait ParticleGenerator {
  def generate(count: Int, geometry: PipeGeometry, parameters: SimulationParameters): Vector[Particle]

  def respawn(particle: Particle, geometry: PipeGeometry, parameters: SimulationParameters): Particle
}
