package io.codeswarm.schauberger.physics

/** Solid-body profile v-theta = omega * r. */
final class SolidBodySwirlProfile extends SwirlProfile {
  override def tangentialVelocity(radialDistance: Double, characteristicRadius: Double, angularVelocity: Double): Double = {
    require(radialDistance >= 0.0 && characteristicRadius > 0.0 && angularVelocity >= 0.0)
    angularVelocity * radialDistance
  }
}
