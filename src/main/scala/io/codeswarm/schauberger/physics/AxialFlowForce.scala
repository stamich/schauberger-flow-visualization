package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.Particle

/** Drives local axial velocity toward the configured target. */
final class AxialFlowForce extends FlowForce {
  /** Computes an acceleration along the local pipe tangent. */
  override def acceleration(particle: Particle, context: FlowContext): Vector3D = {
    val tangent = context.geometry.tangentAt(particle.position).normalized
    val current = particle.velocity.dot(tangent)
    val target = context.parameters.axial.velocity
    tangent * ((target - current) * context.parameters.axial.response)
  }
}
