package io.codeswarm.schauberger.physics.swirl

/** Solid-body vortex profile `vθ = ωr`. */
case object SolidBodySwirlProfile extends SwirlProfile {
  /** Returns velocity growing linearly with radius. */
  override def tangentialVelocity(radialDistance: Double, characteristicRadius: Double, angularVelocity: Double): Double = {
    require(radialDistance >= 0.0 && characteristicRadius > 0.0 && angularVelocity >= 0.0)
    angularVelocity * radialDistance
  }
}
