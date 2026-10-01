package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.math.Vector2D
import io.codeswarm.schauberger.model.Particle

/** Applies a soft inward acceleration close to the upper and lower pipe walls.
  *
  * This is a deliberately simple visualization model rather than a pressure-field
  * solution. Hard boundary correction remains the responsibility of
  * `PipeBoundaryHandler`.
  */
final class WallRepulsionForce extends FlowForce {

  /** Calculates inward y acceleration when a particle enters the wall threshold. */
  override def acceleration(particle: Particle, context: FlowContext): Vector2D = {
    val radius = context.geometry.radius
    val threshold = context.parameters.wallThreshold
    val strength = context.parameters.wallStrength
    val upperDistance = radius - particle.position.y
    val lowerDistance = radius + particle.position.y

    val upperPush = if (upperDistance < threshold) -strength * (threshold - upperDistance) / math.max(threshold, 1e-9) else 0.0
    val lowerPush = if (lowerDistance < threshold) strength * (threshold - lowerDistance) / math.max(threshold, 1e-9) else 0.0

    Vector2D(0.0, upperPush + lowerPush)
  }
}
