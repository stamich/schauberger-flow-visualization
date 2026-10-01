package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.Particle
/** Numerical integration strategy. */
trait ParticleIntegrator { def integrate(particle: Particle, acceleration: Vector3D, deltaTime: Double, maxVelocity: Double): Particle }
