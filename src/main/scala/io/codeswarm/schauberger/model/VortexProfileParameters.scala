package io.codeswarm.schauberger.model

/** Parameters selecting and configuring a tangential vortex profile. */
sealed trait VortexProfileParameters {
  /** Stable identifier used by UI, benchmarks and serialized diagnostics. */
  def id: String
}

/** Solid-body rotation with `vθ = ωr`. */
case object SolidBodyParameters extends VortexProfileParameters {
  override val id: String = "solid-body"
}

/** Rankine vortex parameters.
 *
 * @param coreRadiusRatio core radius divided by the characteristic pipe radius
 */
final case class RankineParameters(coreRadiusRatio: Double) extends VortexProfileParameters {
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
                                    ) extends VortexProfileParameters {
  require(circulation >= 0.0)
  require(coreRadiusRatio > 0.0 && coreRadiusRatio <= 1.0)
  override val id: String = "lamb-oseen"
}
