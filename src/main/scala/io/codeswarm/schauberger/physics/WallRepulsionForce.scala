package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.Particle

/** Geometry-independent soft wall force based on signed boundary distance and inward normal. */
final class WallRepulsionForce extends FlowForce {
  override def acceleration(particle: Particle, context: FlowContext): Vector3D = {
    val threshold = context.parameters.wallThreshold
    val distance = context.geometry.signedDistanceToBoundary(particle.position)
    if (threshold <= 0.0 || distance >= threshold) Vector3D.Zero
    else {
      val penetration = math.max(0.0, threshold - distance)
      context.geometry.inwardNormal(particle.position) * (context.parameters.wallStrength * penetration / threshold)
    }
  }
}
