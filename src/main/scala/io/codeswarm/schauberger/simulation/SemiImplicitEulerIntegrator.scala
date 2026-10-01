package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.Particle

/** Semi-implicit Euler integrator suitable for the lightweight real-time model. */
final class SemiImplicitEulerIntegrator extends ParticleIntegrator {
  /** Updates velocity first and then position using the new velocity. */
  override def integrate(
      particle: Particle,
      acceleration: Vector3D,
      deltaTime: Double,
      maxVelocity: Double
  ): Particle = {
    require(deltaTime >= 0.0, "deltaTime must be non-negative")
    require(maxVelocity > 0.0, "maxVelocity must be positive")
    val newVelocity = (particle.velocity + acceleration * deltaTime).limit(maxVelocity)
    val newPosition = particle.position + newVelocity * deltaTime
    particle.copy(position = newPosition, velocity = newVelocity)
  }
}
