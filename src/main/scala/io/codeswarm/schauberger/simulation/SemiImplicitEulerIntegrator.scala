package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.Particle
/** Semi-implicit Euler integration: update velocity first, then position. */
final class SemiImplicitEulerIntegrator extends ParticleIntegrator {
  override def integrate(particle: Particle, acceleration: Vector3D, deltaTime: Double, maxVelocity: Double): Particle = {
    require(deltaTime >= 0.0 && maxVelocity > 0.0)
    val velocity = (particle.velocity + acceleration * deltaTime).limit(maxVelocity)
    particle.copy(position = particle.position + velocity * deltaTime, velocity = velocity)
  }
}
