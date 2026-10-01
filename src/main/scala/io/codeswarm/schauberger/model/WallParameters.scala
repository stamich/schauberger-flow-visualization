package io.codeswarm.schauberger.model

/** Parameters controlling soft wall repulsion. */
final case class WallParameters(strength: Double, threshold: Double) {
  require(strength >= 0.0)
  require(threshold >= 0.0)
}

/** Default wall-force settings. */
object WallParameters {
  val Default: WallParameters = WallParameters(strength = 85.0, threshold = 16.0)
}
