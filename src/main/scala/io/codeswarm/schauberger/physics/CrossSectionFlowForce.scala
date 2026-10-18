package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.physics.secondary.SecondaryFlowModel
import io.codeswarm.schauberger.physics.swirl.SwirlProfileFactory
import io.codeswarm.schauberger.simulation.ParticleFlowContext

/** Drives the in-plane (cross-sectional) velocity toward one combined target.
 *
 * Swirl and geometry-induced secondary flow both act in the cross-section. As
 * two independent relaxations they fought over the tangential component: the
 * secondary force pulled the whole in-plane velocity, swirl included, toward
 * its own field, so swirl settled at `k_s / (k_s + k_2)` of its target. This
 * force resolves the in-plane velocity on the local radial and tangential axes
 * and relaxes each axis exactly once:
 *
 *  - tangential: toward `v_profile + S_t` with the swirl response (secondary
 *    response when swirl is off), where `S_t` is the tangential part of the
 *    secondary target;
 *  - radial: toward the radial part `S_r` of the secondary target with the
 *    secondary response, only while secondary flow is enabled.
 *
 * With secondary flow disabled this is exactly the swirl relaxation of earlier
 * milestones. The result has no axial component.
 */
final class CrossSectionFlowForce(
                                   profileFactory: SwirlProfileFactory,
                                   secondaryModel: SecondaryFlowModel
                                 ) extends FlowForce {
  /** Computes the in-plane acceleration toward the combined swirl and secondary-flow target. */
  override def acceleration(context: ParticleFlowContext): Vector3D = {
    val parameters = context.flow.parameters
    val swirl = parameters.swirl
    val secondary = parameters.secondaryFlow
    val frame = context.geometry.frame
    val velocity = context.particle.velocity

    val secondaryTarget =
      if (secondary.enabled) {
        frame.crossSectionVectorToWorld(
          secondaryModel.targetVelocity(context.geometry.localPosition, context.flow.geometry, context.particle.position.x, secondary)
        )
      } else Vector3D.Zero

    val radial = frame.crossSectionVectorToWorld(context.geometry.localPosition)
    val radius = radial.magnitude
    if (radius <= Vector3D.Epsilon) {
      // On the axis the radial and tangential axes are undefined; relax the in-plane velocity as a whole.
      if (!secondary.enabled) Vector3D.Zero
      else {
        val inPlane = frame.crossSectionVectorToWorld(frame.worldVectorToCrossSection(velocity))
        (secondaryTarget - inPlane) * secondary.response
      }
    } else {
      val radialUnit = radial / radius
      val tangentialUnit = frame.tangent.normalized.cross(radialUnit)

      val swirlActive = swirl.angularVelocity > 0.0
      val swirlTarget =
        if (swirlActive) {
          val speed = profileFactory.create(swirl.profile).tangentialVelocity(radius, context.flow.geometry.characteristicRadius, swirl.angularVelocity)
          speed * swirl.rotationDirection.sign
        } else 0.0
      val tangentialResponse = if (swirlActive) swirl.response else if (secondary.enabled) secondary.response else 0.0
      val tangentialTarget = swirlTarget + secondaryTarget.dot(tangentialUnit)
      val tangentialAcceleration = tangentialResponse * (tangentialTarget - velocity.dot(tangentialUnit))

      val radialAcceleration =
        if (secondary.enabled) secondary.response * (secondaryTarget.dot(radialUnit) - velocity.dot(radialUnit))
        else 0.0

      tangentialUnit * tangentialAcceleration + radialUnit * radialAcceleration
    }
  }
}
