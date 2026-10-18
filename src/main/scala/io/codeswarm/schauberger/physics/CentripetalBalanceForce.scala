package io.codeswarm.schauberger.physics

import io.codeswarm.schauberger.math.Vector3D
import io.codeswarm.schauberger.simulation.ParticleFlowContext

/** Supplies the radial pressure-gradient acceleration `a_r = -v_theta^2 / r` of a swirling flow.
 *
 * Without it a tracer that has acquired tangential speed moves along a straight
 * tangent and drifts toward the wall, which is the opposite of the inward
 * motion a vortex shows. The acceleration points to the local cross-section
 * center and depends only on the tangential speed, so it is independent of the
 * rotation direction and has no axial component.
 *
 * The radius is floored at a small fraction of the characteristic radius so a
 * tracer passing close to the axis cannot receive an unbounded acceleration.
 */
final class CentripetalBalanceForce extends FlowForce {
  /** Computes the inward acceleration that keeps the current tangential motion on a circular orbit. */
  override def acceleration(context: ParticleFlowContext): Vector3D = {
    val parameters = context.flow.parameters.radialBalance
    if (!parameters.enabled || parameters.strength <= 0.0) Vector3D.Zero
    else {
      val frame = context.geometry.frame
      val radial = frame.crossSectionVectorToWorld(context.geometry.localPosition)
      val radius = radial.magnitude
      if (radius <= Vector3D.Epsilon) Vector3D.Zero
      else {
        val radialUnit = radial / radius
        val tangentialUnit = frame.tangent.normalized.cross(radialUnit)
        val tangentialSpeed = context.particle.velocity.dot(tangentialUnit)
        val effectiveRadius = math.max(radius, CentripetalBalanceForce.RadiusFloorRatio * context.flow.geometry.characteristicRadius)
        radialUnit * (-parameters.strength * tangentialSpeed * tangentialSpeed / effectiveRadius)
      }
    }
  }
}

/** Constants of [[CentripetalBalanceForce]]. */
object CentripetalBalanceForce {
  /** Smallest radius, relative to the characteristic radius, used to evaluate the balance. */
  val RadiusFloorRatio: Double = 0.05
}
