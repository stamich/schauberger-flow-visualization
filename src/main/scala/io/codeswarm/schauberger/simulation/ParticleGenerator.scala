package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.{Particle, SimulationParameters}

/** Creates the initial particle population for a simulation. */
trait ParticleGenerator {

  /** Generates particles that satisfy the supplied geometry and parameters. */
  def generate(count: Int, geometry: PipeGeometry, parameters: SimulationParameters): Vector[Particle]
}
