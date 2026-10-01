package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.Particle

/** Drives tangential velocity around the local pipe tangent toward a profile target. */
final class SwirlForce(profile: SwirlProfile) extends FlowForce {
  /** Computes the tangential acceleration contribution. */
  override def acceleration(particle: Particle, context: FlowContext): Vector3D = {
    val center = context.geometry.centerLinePosition(particle.position.x)
    val tangent = context.geometry.tangentAt(particle.position).normalized
    val raw = particle.position - center
    val radial = raw - tangent * raw.dot(tangent)
    val r = radial.magnitude
    val params = context.parameters.swirl
    if (r <= Vector3D.Epsilon || params.angularVelocity <= 0.0) Vector3D.Zero
    else {
      val tangential = tangent.cross(radial).normalized * params.rotationDirection.sign
      val current = particle.velocity.dot(tangential)
      val target = profile.tangentialVelocity(r, context.geometry.characteristicRadius, params.angularVelocity)
      tangential * ((target - current) * params.response)
    }
  }
}
