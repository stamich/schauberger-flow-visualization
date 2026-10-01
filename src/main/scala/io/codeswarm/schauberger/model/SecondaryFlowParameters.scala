package io.codeswarm.schauberger.model

/** Parameters for geometry-induced secondary flow in the local cross-section. */
final case class SecondaryFlowParameters(
    enabled: Boolean,
    strength: Double,
    response: Double,
    boundaryFadeDistance: Double
) {
  require(strength >= 0.0)
  require(response >= 0.0)
  require(boundaryFadeDistance >= 0.0)
}

/** Default secondary-flow settings. */
object SecondaryFlowParameters {
  val Default: SecondaryFlowParameters = SecondaryFlowParameters(
    enabled = true,
    strength = 18.0,
    response = 1.5,
    boundaryFadeDistance = 18.0
  )
}
