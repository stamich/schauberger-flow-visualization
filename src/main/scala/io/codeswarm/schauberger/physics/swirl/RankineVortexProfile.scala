package io.codeswarm.schauberger.physics.swirl

/** Rankine vortex combining a solid-body core and free-vortex exterior.
  *
  * @param coreRadiusRatio core radius divided by characteristic pipe radius
  */
final case class RankineVortexProfile(coreRadiusRatio: Double) extends SwirlProfile {
  require(coreRadiusRatio > 0.0 && coreRadiusRatio <= 1.0)

  /** Evaluates the continuous piecewise Rankine velocity profile. */
  override def tangentialVelocity(radialDistance: Double, characteristicRadius: Double, angularVelocity: Double): Double = {
    require(radialDistance >= 0.0 && characteristicRadius > 0.0 && angularVelocity >= 0.0)
    val coreRadius = math.max(characteristicRadius * coreRadiusRatio, 1e-9)
    if (radialDistance <= coreRadius) angularVelocity * radialDistance
    else angularVelocity * coreRadius * coreRadius / math.max(radialDistance, 1e-9)
  }
}
