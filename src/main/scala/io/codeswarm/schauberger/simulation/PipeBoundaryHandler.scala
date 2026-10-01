package io.codeswarm.schauberger.simulation

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.math.Vector2D
import io.codeswarm.schauberger.model.{Particle, SimulationParameters}

/** Boundary policy for a continuously flowing straight pipe.
  *
  * Outlet particles are moved back to the inlet while retaining identity. Particles
  * that cross a wall are clamped and only the outward vertical velocity component
  * is removed.
  */
final class PipeBoundaryHandler extends BoundaryHandler {

  /** Applies outlet respawn and top/bottom wall correction. */
  override def handle(
      particle: Particle,
      geometry: PipeGeometry,
      parameters: SimulationParameters
  ): Particle = {
    val afterOutlet =
      if (particle.position.x > geometry.length)
        particle.copy(
          position = Vector2D(0.0, math.max(-geometry.radius * 0.92, math.min(geometry.radius * 0.92, particle.position.y))),
          velocity = Vector2D(parameters.axialVelocity * 0.85, particle.velocity.y * 0.25)
        )
      else if (particle.position.x < 0.0)
        particle.copy(position = particle.position.copy(x = 0.0), velocity = particle.velocity.copy(x = math.max(0.0, particle.velocity.x)))
      else particle

    val clamped = geometry.clampToWalls(afterOutlet.position)
    val crossedUpper = afterOutlet.position.y > geometry.radius
    val crossedLower = afterOutlet.position.y < -geometry.radius
    val correctedVy =
      if (crossedUpper && afterOutlet.velocity.y > 0.0) 0.0
      else if (crossedLower && afterOutlet.velocity.y < 0.0) 0.0
      else afterOutlet.velocity.y

    afterOutlet.copy(position = clamped, velocity = afterOutlet.velocity.copy(y = correctedVy))
  }
}
