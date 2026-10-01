package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.Particle
/** Drives velocity along the local tangent toward the configured axial target. */
final class AxialFlowForce extends FlowForce {
  override def acceleration(particle: Particle, context: FlowContext): Vector3D = {
    val tangent = context.geometry.tangentAt(particle.position).normalized
    val current = particle.velocity.dot(tangent)
    tangent * ((context.parameters.axialVelocity - current) * context.parameters.axialResponse)
  }
}
