package io.codeswarm.schauberger.model

/** Render-only settings kept separate from the physical simulation. */
final case class VisualizationParameters(
    viewMode: ViewMode,
    trailLength: Int,
    trailDurationSeconds: Double,
    trailSampleEveryFrames: Int,
    crossSectionFraction: Double,
    crossSectionSliceHalfWidth: Double
) {
  require(trailLength >= 0)
  require(trailDurationSeconds >= 0.0)
  require(trailSampleEveryFrames > 0)
  require(crossSectionFraction >= 0.0 && crossSectionFraction <= 1.0)
  require(crossSectionSliceHalfWidth > 0.0)
}

/** Defaults tuned to keep trails readable instead of accumulating indefinitely. */
object VisualizationParameters {
  val Default: VisualizationParameters = VisualizationParameters(
    viewMode = ViewMode.Longitudinal,
    trailLength = 48,
    trailDurationSeconds = 2.0,
    trailSampleEveryFrames = 4,
    crossSectionFraction = 0.5,
    crossSectionSliceHalfWidth = 24.0
  )
}
