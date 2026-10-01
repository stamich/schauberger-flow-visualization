package io.codeswarm.schauberger.physics

/** Defines target tangential speed as a function of radial position. */
trait SwirlProfile {
  /** Returns unsigned target tangential velocity at the supplied radius. */
  def tangentialVelocity(
      radialDistance: Double,
      pipeRadius: Double,
      angularVelocity: Double
  ): Double
}
