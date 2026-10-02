package io.codeswarm.schauberger.model

import io.codeswarm.schauberger.diagnostics.FieldType

/** Render-only settings kept separate from physical simulation parameters. */
final case class VisualizationParameters(
                                          viewMode: ViewMode,
                                          trailLength: Int,
                                          trailDurationSeconds: Double,
                                          trailSampleEveryFrames: Int,
                                          crossSectionFraction: Double,
                                          crossSectionSliceHalfWidth: Double,
                                          showParticles: Boolean,
                                          showTrails: Boolean,
                                          showSecondaryVectors: Boolean,
                                          vectorFieldResolution: Int,
                                          showHeatMap: Boolean,
                                          heatMapField: FieldType,
                                          heatMapResolution: Int
                                        ) {
  require(trailLength >= 0)
  require(trailDurationSeconds >= 0.0)
  require(trailSampleEveryFrames > 0)
  require(crossSectionFraction >= 0.0 && crossSectionFraction <= 1.0)
  require(crossSectionSliceHalfWidth > 0.0)
  require(vectorFieldResolution >= 3 && vectorFieldResolution <= 80)
  require(heatMapResolution >= 5 && heatMapResolution <= 80)
}

/** Defaults balancing readability and diagnostic detail. */
object VisualizationParameters {
  val Default: VisualizationParameters = VisualizationParameters(
    viewMode = ViewMode.Longitudinal,
    trailLength = 48,
    trailDurationSeconds = 2.0,
    trailSampleEveryFrames = 4,
    crossSectionFraction = 0.5,
    crossSectionSliceHalfWidth = 24.0,
    showParticles = true,
    showTrails = true,
    showSecondaryVectors = false,
    vectorFieldResolution = 15,
    showHeatMap = true,
    heatMapField = FieldType.VelocityMagnitude,
    heatMapResolution = 30
  )
}
