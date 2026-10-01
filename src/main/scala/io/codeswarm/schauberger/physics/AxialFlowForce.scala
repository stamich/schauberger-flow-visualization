package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.simulation.ParticleFlowContext

/** Drives local axial velocity toward the configured target. */
final class AxialFlowForce extends FlowForce {
  /** Computes an acceleration along the cached local pipe tangent. */
  override def acceleration(context: ParticleFlowContext): Vector3D = {
    val tangent = context.geometry.frame.tangent.normalized
    val current = context.particle.velocity.dot(tangent)
    val target = context.flow.parameters.axial.velocity
    tangent * ((target - current) * context.flow.parameters.axial.response)
  }
}
