package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.Particle

/** Applies a soft inward radial acceleration close to the circular pipe wall.
  *
  * This is a visualization-oriented confinement force, not a solved pressure field.
  * Hard correction remains the responsibility of the boundary handler.
  */
final class WallRepulsionForce extends FlowForce {
  /** Returns zero away from the wall and an inward radial acceleration near it. */
  override def acceleration(particle: Particle, context: FlowContext): Vector3D = {
    val center = context.geometry.centerLinePosition(particle.position.x)
    val tangent = context.geometry.tangentAt(particle.position).normalized
    val rawRadial = particle.position - center
    val radial = rawRadial - tangent * rawRadial.dot(tangent)
    val distance = radial.magnitude
    val distanceFromWall = context.geometry.radius - distance
    val threshold = context.parameters.wallThreshold

    if (distance <= Vector3D.Epsilon || threshold <= 0.0 || distanceFromWall >= threshold) Vector3D.Zero
    else {
      val penetration = math.max(0.0, threshold - distanceFromWall)
      val magnitude = context.parameters.wallStrength * penetration / threshold
      radial.normalized * -magnitude
    }
  }
}
