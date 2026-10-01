package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.simulation.ParticleFlowContext

/** Geometry-independent soft wall force using precomputed boundary data. */
final class WallRepulsionForce extends FlowForce {
  /** Computes an inward acceleration near the current geometry boundary. */
  override def acceleration(context: ParticleFlowContext): Vector3D = {
    val threshold = context.flow.parameters.wall.threshold
    val distance = context.geometry.signedBoundaryDistance
    if (threshold <= 0.0 || distance >= threshold) Vector3D.Zero
    else {
      val penetration = math.max(0.0, threshold - distance)
      context.geometry.inwardNormal * (context.flow.parameters.wall.strength * penetration / threshold)
    }
  }
}
