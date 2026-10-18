package io.codeswarm.schauberger.model

/** Rotation sense of a particle relative to the configured swirl direction. */
sealed trait VortexRegion

/** Classification of particles by their angular velocity. */
object VortexRegion {
  /** Turning with the configured rotation direction. */
  case object Forward extends VortexRegion

  /** Turning against the configured rotation direction, as in the outer layer of a counter-rotating vortex. */
  case object Reverse extends VortexRegion

  /** Angular velocity too small, compared with the configured one, to call a direction. */
  case object Quiescent extends VortexRegion

  /** Fraction of the configured angular velocity below which a particle counts as quiescent. */
  val DeadBand: Double = 0.05

  /** Classifies one particle.
   *
   * @param angularVelocity   the particle's angular velocity about the axis, positive along the configured direction
   * @param referenceVelocity the configured swirl angular velocity; when it is not positive nothing is swirling
   */
  def classify(angularVelocity: Double, referenceVelocity: Double): VortexRegion =
    if (referenceVelocity <= 0.0 || math.abs(angularVelocity) <= DeadBand * referenceVelocity) Quiescent
    else if (angularVelocity > 0.0) Forward
    else Reverse
}
