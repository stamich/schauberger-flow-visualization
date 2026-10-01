package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.math.Vector2D
import io.codeswarm.schauberger.model.Particle

/** Integrates particle motion over a time step. */
trait ParticleIntegrator {

  /** Produces the next particle state from acceleration and elapsed simulation time.
    *
    * @param particle current particle state
    * @param acceleration acceleration acting during the step
    * @param deltaTime step duration in seconds
    */
  def integrate(particle: Particle, acceleration: Vector2D, deltaTime: Double): Particle
}
