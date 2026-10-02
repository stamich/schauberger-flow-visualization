package io.codeswarm.schauberger.physics.swirl

/** Smooth Lamb-Oseen-inspired vortex profile with a finite viscous core.
 *
 * The implementation scales circulation from the existing simulation angular
 * velocity so the profile remains comparable with the other visualization
 * profiles rather than claiming SI-calibrated circulation.
 *
 * @param circulation     dimensionless circulation multiplier
 * @param coreRadiusRatio core radius divided by characteristic pipe radius
 */
final case class LambOseenVortexProfile(
                                         circulation: Double,
                                         coreRadiusRatio: Double
                                       ) extends SwirlProfile {
  require(circulation >= 0.0)
  require(coreRadiusRatio > 0.0 && coreRadiusRatio <= 1.0)

  /** Evaluates a numerically stable Lamb-Oseen-shaped tangential velocity. */
  override def tangentialVelocity(radialDistance: Double, characteristicRadius: Double, angularVelocity: Double): Double = {
    require(radialDistance >= 0.0 && characteristicRadius > 0.0 && angularVelocity >= 0.0)
    if (radialDistance <= 1e-9 || circulation == 0.0 || angularVelocity == 0.0) 0.0
    else {
      val coreRadius = math.max(characteristicRadius * coreRadiusRatio, 1e-9)
      val normalized = radialDistance / coreRadius
      val shape = -math.expm1(-(normalized * normalized)) / normalized
      angularVelocity * characteristicRadius * circulation * shape
    }
  }
}
