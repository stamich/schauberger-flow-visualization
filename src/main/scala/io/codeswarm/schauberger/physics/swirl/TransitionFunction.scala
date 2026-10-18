package io.codeswarm.schauberger.physics.swirl

/** Smooth blend between an inner and an outer layer of a composite vortex. */
trait TransitionFunction {
  /** Returns the weight of the inner layer at normalized radius `rho = r / R`.
   *
   * The result lies in `[0, 1]`: close to 1 near the axis, close to 0 near the
   * wall and non-increasing in between. The weight of the outer layer is
   * `1 - weight(rho)`.
   */
  def innerWeight(normalizedRadius: Double): Double
}

/** Hyperbolic-tangent transition `w = (1 - tanh((rho - center) / width)) / 2`.
 *
 * Unlike a hard switch it is smooth, so the blended velocity has no jump and
 * the vorticity stays finite across the interface.
 *
 * @param center normalized radius at which the weight is one half, in `(0, 1)`
 * @param width  normalized half-width of the transition zone, greater than zero
 */
final case class TanhTransition(center: Double, width: Double) extends TransitionFunction {
  require(center > 0.0 && center < 1.0, "transition center must lie inside the pipe")
  require(width > 0.0, "transition width must be positive")

  /** Evaluates the tanh weight. */
  override def innerWeight(normalizedRadius: Double): Double =
    0.5 * (1.0 - math.tanh((normalizedRadius - center) / width))
}
