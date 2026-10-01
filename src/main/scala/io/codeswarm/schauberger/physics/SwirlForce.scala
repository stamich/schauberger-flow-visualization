package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.model.Particle

/** Drives tangential velocity toward the value requested by a [[SwirlProfile]].
  *
  * The implementation does not add an unbounded constant angular acceleration.
  * Instead it computes the local tangential direction and applies a first-order
  * correction toward the target tangential speed.
  *
  * @param profile radial profile defining the desired tangential speed
  */
final class SwirlForce(profile: SwirlProfile) extends FlowForce {

  /** Calculates tangential acceleration around the local pipe tangent. */
  override def acceleration(particle: Particle, context: FlowContext): Vector3D = {
    val center = context.geometry.centerLinePosition(particle.position.x)
    val tangent = context.geometry.tangentAt(particle.position).normalized
    val radialRaw = particle.position - center
    val radial = radialRaw - tangent * radialRaw.dot(tangent)
    val radius = radial.magnitude

    if (radius <= Vector3D.Epsilon || context.parameters.angularVelocity <= 0.0) Vector3D.Zero
    else {
      val baseTangential = tangent.cross(radial).normalized
      val tangentialDirection = baseTangential * context.parameters.rotationDirection.sign
      val currentTangentialVelocity = particle.velocity.dot(tangentialDirection)
      val targetTangentialVelocity = profile.tangentialVelocity(
        radialDistance = radius,
        pipeRadius = context.geometry.radius,
        angularVelocity = context.parameters.angularVelocity
      )
      val delta = targetTangentialVelocity - currentTangentialVelocity
      tangentialDirection * (delta * context.parameters.swirlResponse)
    }
  }
}
