package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.math.Vector2D
import io.codeswarm.schauberger.model.Particle

/** Semi-implicit Euler integrator.
  *
  * Velocity is updated first and the new velocity is then used to update position.
  * This simple method is suitable for the milestone 0.1 baseline and is generally
  * more stable than explicit Euler for interactive simulations.
  */
final class SemiImplicitEulerIntegrator extends ParticleIntegrator {

  /** Advances one particle by one simulation step. */
  override def integrate(particle: Particle, acceleration: Vector2D, deltaTime: Double): Particle = {
    require(deltaTime >= 0.0, "deltaTime must be non-negative")
    val nextVelocity = particle.velocity + acceleration * deltaTime
    val nextPosition = particle.position + nextVelocity * deltaTime
    particle.copy(position = nextPosition, velocity = nextVelocity)
  }
}
