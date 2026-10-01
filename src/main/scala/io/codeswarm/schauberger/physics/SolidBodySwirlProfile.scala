package io.codeswarm.schauberger.physics

/** Solid-body rotation profile where tangential speed grows linearly with radius.
  *
  * The profile follows v_theta = omega * r. It is deliberately chosen as the first
  * model because it is stable, easy to explain and easy to validate visually.
  */
final class SolidBodySwirlProfile extends SwirlProfile {
  /** Calculates v-theta = angularVelocity * radialDistance. */
  override def tangentialVelocity(
      radialDistance: Double,
      pipeRadius: Double,
      angularVelocity: Double
  ): Double = {
    require(radialDistance >= 0.0, "radialDistance must be non-negative")
    require(pipeRadius > 0.0, "pipeRadius must be positive")
    require(angularVelocity >= 0.0, "angularVelocity must be non-negative")
    angularVelocity * radialDistance
  }
}
