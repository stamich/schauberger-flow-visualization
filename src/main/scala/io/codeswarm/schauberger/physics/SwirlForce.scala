package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.simulation.ParticleFlowContext

/** Drives tangential velocity around the local pipe tangent toward a profile target. */
final class SwirlForce(profile: SwirlProfile) extends FlowForce {
  /** Computes tangential acceleration using cached frame and local position. */
  override def acceleration(context: ParticleFlowContext): Vector3D = {
    val tangent = context.geometry.frame.tangent.normalized
    val radial = context.geometry.frame.crossSectionVectorToWorld(context.geometry.localPosition)
    val radius = radial.magnitude
    val parameters = context.flow.parameters.swirl
    if (radius <= Vector3D.Epsilon || parameters.angularVelocity <= 0.0) Vector3D.Zero
    else {
      val tangential = tangent.cross(radial).normalized * parameters.rotationDirection.sign
      val current = context.particle.velocity.dot(tangential)
      val target = profile.tangentialVelocity(radius, context.flow.geometry.characteristicRadius, parameters.angularVelocity)
      tangential * ((target - current) * parameters.response)
    }
  }
}
