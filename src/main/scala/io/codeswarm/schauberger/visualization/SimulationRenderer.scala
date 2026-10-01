package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.{SimulationParameters, SimulationState, ViewMode, VisualizationParameters}
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color

/** Coordinates heat maps, geometry, trails, particles and vector fields on one Canvas. */
final class SimulationRenderer(
    canvas: Canvas,
    pipeRenderer: PipeRenderer,
    particleRenderer: ParticleRenderer,
    trailRenderer: TrailRenderer,
    trailBuffer: TrailBuffer,
    vectorFieldRenderer: VectorFieldRenderer,
    heatMapRenderer: HeatMapRenderer
) {
  /** Renders one complete frame using current physical and visualization parameters. */
  def render(state: SimulationState, geometry: PipeGeometry, simulation: SimulationParameters, visualization: VisualizationParameters): Unit = {
    val graphics = canvas.graphicsContext2D
    graphics.fill = Color.web("#0f1720")
    graphics.fillRect(0.0, 0.0, canvas.width.value, canvas.height.value)

    val sliceX = geometry.length * visualization.crossSectionFraction
    val (projection, transform) = visualization.viewMode match {
      case ViewMode.Longitudinal =>
        (new LongitudinalProjection: Projection, ViewportTransform(0.0, geometry.length, -geometry.boundingRadius, geometry.boundingRadius, canvas.width.value, canvas.height.value))
      case ViewMode.CrossSection =>
        val margin = geometry.boundingRadius * 1.08
        (new CrossSectionProjection: Projection, ViewportTransform(-margin, margin, -margin, margin, canvas.width.value, canvas.height.value))
    }

    trailBuffer.record(state, visualization.trailLength, visualization.trailDurationSeconds, visualization.trailSampleEveryFrames)

    if (visualization.viewMode == ViewMode.CrossSection && visualization.showHeatMap) {
      heatMapRenderer.render(graphics, state, geometry, transform, sliceX, visualization)
    }
    if (visualization.viewMode == ViewMode.CrossSection && visualization.showSecondaryVectors) {
      vectorFieldRenderer.render(graphics, geometry, transform, sliceX, simulation.secondaryFlow, visualization.vectorFieldResolution)
    }
    if (visualization.showTrails) trailRenderer.render(graphics, state, trailBuffer, projection, transform, visualization, sliceX)
    if (visualization.showParticles) particleRenderer.render(graphics, state, geometry, projection, transform, visualization, sliceX)
    pipeRenderer.render(graphics, geometry, transform, visualization, sliceX)
  }

  /** Clears all accumulated render-only trajectory history. */
  def clearTrails(): Unit = trailBuffer.clear()
}
