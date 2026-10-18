package io.codeswarm.schauberger.physics.swirl

/** Signed blend of an inner layer turning with the rotation direction and an outer layer turning against it.
 *
 * `v = s_i * w(rho) * v_inner(r) - s_o * (1 - w(rho)) * v_outer(r)`, where `w`
 * is the inner weight of the transition and `rho = r / characteristicRadius`.
 * Positive values turn with the configured rotation direction, negative values
 * against it. The velocity is zero on the axis whenever both layers are.
 *
 * @param inner         central layer
 * @param outer         outer layer
 * @param innerStrength multiplier of the inner layer
 * @param outerStrength multiplier of the outer layer
 * @param transition    blend between the layers
 */
final case class CounterRotatingVortexProfile(
                                               inner: SwirlProfile,
                                               outer: SwirlProfile,
                                               innerStrength: Double,
                                               outerStrength: Double,
                                               transition: TransitionFunction
                                             ) extends SwirlProfile {
  require(innerStrength >= 0.0 && outerStrength >= 0.0, "layer strengths must not be negative")

  /** Evaluates the signed blended tangential speed. */
  override def tangentialVelocity(radialDistance: Double, characteristicRadius: Double, angularVelocity: Double): Double = {
    require(radialDistance >= 0.0 && characteristicRadius > 0.0 && angularVelocity >= 0.0)
    val weight = transition.innerWeight(radialDistance / characteristicRadius)
    val innerSpeed = inner.tangentialVelocity(radialDistance, characteristicRadius, angularVelocity)
    val outerSpeed = outer.tangentialVelocity(radialDistance, characteristicRadius, angularVelocity)
    innerStrength * weight * innerSpeed - outerStrength * (1.0 - weight) * outerSpeed
  }
}
