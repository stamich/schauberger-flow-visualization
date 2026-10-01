package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.{Particle, SimulationParameters}

/** Geometry-aware finite-pipe boundary policy with outlet-to-inlet recirculation. */
final class PipeBoundaryHandler extends BoundaryHandler {
  override def handle(particle: Particle, geometry: PipeGeometry, parameters: SimulationParameters, generator: ParticleGenerator): Particle = {
    if (particle.position.x > geometry.length) generator.respawn(particle, geometry, parameters)
    else {
      val xCorrected = if (particle.position.x < 0.0) particle.position.copy(x = 0.0) else particle.position
      val correctedPosition = if (geometry.crossSection.contains(geometry.toLocalCrossSection(xCorrected))) xCorrected else geometry.clampInside(xCorrected)
      if (correctedPosition == xCorrected) particle.copy(position = xCorrected)
      else {
        val inward = geometry.inwardNormal(correctedPosition)
        val outward = inward * -1.0
        val outwardSpeed = particle.velocity.dot(outward)
        val velocity = if (outwardSpeed > 0.0) particle.velocity - outward * outwardSpeed else particle.velocity
        particle.copy(position = correctedPosition, velocity = velocity)
      }
    }
  }
}
