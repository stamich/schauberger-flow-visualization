package io.codeswarm.schauberger.physics.swirl

/** Computes target tangential speed as a function of radial distance. */
trait SwirlProfile {
  /** Returns the target tangential speed at `radialDistance`. */
  def tangentialVelocity(
      radialDistance: Double,
      characteristicRadius: Double,
      angularVelocity: Double
  ): Double
}
