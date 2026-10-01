package io.codeswarm.schauberger.physics.secondary

/** Scales a secondary-flow field near solid walls. */
trait BoundaryAttenuation {
  /** Returns a factor in `[0, 1]` from signed inside distance and fade distance. */
  def factor(signedDistance: Double, fadeDistance: Double): Double
}
