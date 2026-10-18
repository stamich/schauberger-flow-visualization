package io.codeswarm.schauberger.model

/** Parameters selecting and configuring a tangential vortex profile. */
sealed trait VortexProfileParameters {
  /** Stable identifier used by UI, benchmarks and serialized diagnostics. */
  def id: String
}

/** A profile that is a single smooth vortex.
 *
 * Composite profiles combine two of these layers. Restricting the layers to
 * this type keeps composites one level deep, so a composite can never contain
 * another composite and the factory needs no recursion.
 */
sealed trait SimpleVortexProfileParameters extends VortexProfileParameters

/** Solid-body rotation with `vθ = ωr`. */
case object SolidBodyParameters extends SimpleVortexProfileParameters {
  override val id: String = "solid-body"
}

/** Rankine vortex parameters.
 *
 * @param coreRadiusRatio core radius divided by the characteristic pipe radius
 */
final case class RankineParameters(coreRadiusRatio: Double) extends SimpleVortexProfileParameters {
  require(coreRadiusRatio > 0.0 && coreRadiusRatio <= 1.0)
  override val id: String = "rankine"
}

/** Lamb-Oseen vortex parameters.
 *
 * @param circulation     dimensionless circulation multiplier applied to the base angular scale
 * @param coreRadiusRatio viscous-core radius divided by the characteristic pipe radius
 */
final case class LambOseenParameters(
                                      circulation: Double,
                                      coreRadiusRatio: Double
                                    ) extends SimpleVortexProfileParameters {
  require(circulation >= 0.0)
  require(coreRadiusRatio > 0.0 && coreRadiusRatio <= 1.0)
  override val id: String = "lamb-oseen"
}

/** Two vortex layers rotating in opposite senses, blended by a smooth transition.
 *
 * The inner layer turns with the configured rotation direction and the outer
 * layer against it. Strengths are multipliers on each layer's own profile, so
 * the zero crossing of the blended velocity lies at the transition radius only
 * when the two contributions are equal there; for unequal strengths it moves
 * toward the weaker layer. The default outer layer is solid-body because two
 * Rankine layers would leave an almost irrotational outer region.
 *
 * @param inner            profile of the central layer
 * @param outer            profile of the outer layer
 * @param innerStrength    multiplier of the inner layer, not negative
 * @param outerStrength    multiplier of the outer layer, not negative
 * @param transitionRadius normalized radius `r / R` of the half-way point of the blend, in `(0, 1)`
 * @param transitionWidth  normalized half-width of the blend, greater than zero
 */
final case class CounterRotatingParameters(
                                            inner: SimpleVortexProfileParameters = RankineParameters(0.4),
                                            outer: SimpleVortexProfileParameters = SolidBodyParameters,
                                            innerStrength: Double = 1.0,
                                            outerStrength: Double = 0.6,
                                            transitionRadius: Double = 0.55,
                                            transitionWidth: Double = 0.08
                                          ) extends VortexProfileParameters {
  require(innerStrength >= 0.0 && outerStrength >= 0.0, "layer strengths must not be negative")
  require(transitionRadius > 0.0 && transitionRadius < 1.0, "transition radius must lie inside the pipe")
  require(transitionWidth > 0.0, "transition width must be positive")
  override val id: String = "counter-rotating"
}
