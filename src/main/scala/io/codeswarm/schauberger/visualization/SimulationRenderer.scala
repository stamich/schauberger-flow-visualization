package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.{SimulationState, ViewMode, VisualizationParameters}
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color

/** Coordinates geometry, fading trails and particles on one ScalaFX Canvas. */
final class SimulationRenderer(
    canvas: Canvas,
    pipeRenderer: PipeRenderer,
    particleRenderer: ParticleRenderer,
    trailRenderer: TrailRenderer,
    trailBuffer: TrailBuffer
) {

  /** Renders one complete frame using the selected projection and cross-section slice. */
  def render(
      state: SimulationState,
      geometry: PipeGeometry,
      visualization: VisualizationParameters
  ): Unit = {
    val graphics = canvas.graphicsContext2D
    graphics.fill = Color.web("#0f1720")
    graphics.fillRect(0.0, 0.0, canvas.width.value, canvas.height.value)

    val sliceX = geometry.length * visualization.crossSectionFraction
    val (projection, transform) = visualization.viewMode match {
      case ViewMode.Longitudinal =>
        (
          new LongitudinalProjection: Projection,
          ViewportTransform(
            0.0,
            geometry.length,
            -geometry.boundingRadius,
            geometry.boundingRadius,
            canvas.width.value,
            canvas.height.value
          )
        )
      case ViewMode.CrossSection =>
        val margin = geometry.boundingRadius * 1.08
        (
          new CrossSectionProjection: Projection,
          ViewportTransform(-margin, margin, -margin, margin, canvas.width.value, canvas.height.value)
        )
    }

    trailBuffer.record(
      state,
      visualization.trailLength,
      visualization.trailDurationSeconds,
      visualization.trailSampleEveryFrames
    )
    pipeRenderer.render(graphics, geometry, transform, visualization, sliceX)
    trailRenderer.render(graphics, state, trailBuffer, projection, transform, visualization, sliceX)
    particleRenderer.render(graphics, state, geometry, projection, transform, visualization, sliceX)
  }

  /** Clears all accumulated render-only trajectory history. */
  def clearTrails(): Unit = trailBuffer.clear()
}
