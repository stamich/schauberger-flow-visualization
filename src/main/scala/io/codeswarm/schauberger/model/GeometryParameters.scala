package io.codeswarm.schauberger.model

/** Parameters used to construct the active pipe geometry. */
final case class GeometryParameters(
    geometryType: GeometryType,
    length: Double,
    width: Double,
    height: Double,
    asymmetry: Double,
    twistTurns: Double
) {
  require(length > 0.0 && width > 0.0 && height > 0.0)
  require(asymmetry >= -0.35 && asymmetry <= 0.35)
  require(twistTurns >= 0.0)
}

/** Readable defaults showcasing the new twisted ovoid geometry. */
object GeometryParameters {
  val Default: GeometryParameters = GeometryParameters(
    geometryType = GeometryType.TwistedOvoid,
    length = 1000.0,
    width = 220.0,
    height = 250.0,
    asymmetry = 0.14,
    twistTurns = 1.25
  )
}
