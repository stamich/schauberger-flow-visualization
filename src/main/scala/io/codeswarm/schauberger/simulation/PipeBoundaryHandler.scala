package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.{Particle, SimulationParameters}

/** Boundary policy for a finite pipe with outlet-to-inlet recirculation. */
final class PipeBoundaryHandler extends BoundaryHandler {

  /** Respawns particles past the outlet and removes outward wall-normal velocity after clamping. */
  override def handle(
      particle: Particle,
      geometry: PipeGeometry,
      parameters: SimulationParameters,
      generator: ParticleGenerator
  ): Particle = {
    if (particle.position.x > geometry.length) generator.respawn(particle, geometry, parameters)
    else {
      val xCorrected = if (particle.position.x < 0.0) particle.position.copy(x = 0.0) else particle.position
      val radiallyClamped = geometry.clampToWalls(xCorrected)
      val wasClamped = radiallyClamped != xCorrected
      if (!wasClamped) particle.copy(position = xCorrected)
      else {
        val center = geometry.centerLinePosition(radiallyClamped.x)
        val tangent = geometry.tangentAt(radiallyClamped).normalized
        val rawRadial = radiallyClamped - center
        val radial = rawRadial - tangent * rawRadial.dot(tangent)
        val normal = radial.normalized
        val outwardSpeed = particle.velocity.dot(normal)
        val correctedVelocity =
          if (outwardSpeed > 0.0) particle.velocity - normal * outwardSpeed
          else particle.velocity
        particle.copy(position = radiallyClamped, velocity = correctedVelocity)
      }
    }
  }
}
