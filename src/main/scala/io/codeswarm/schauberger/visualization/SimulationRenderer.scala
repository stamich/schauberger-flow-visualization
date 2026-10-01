package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.{SimulationParameters, SimulationState, ViewMode}
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color

/** Facade coordinating milestone 0.2 Canvas rendering and render-only trail history. */
final class SimulationRenderer(
    canvas: Canvas,
    pipeRenderer: PipeRenderer,
    particleRenderer: ParticleRenderer,
    trailRenderer: TrailRenderer,
    trailBuffer: TrailBuffer
) {

  /** Renders the complete frame using the selected view and current trail length. */
  def render(
      state: SimulationState,
      geometry: PipeGeometry,
      parameters: SimulationParameters,
      viewMode: ViewMode
  ): Unit = {
    val gc = canvas.graphicsContext2D
    gc.fill = Color.web("#0f1720")
    gc.fillRect(0.0, 0.0, canvas.width.value, canvas.height.value)

    val (projection, transform) = viewMode match {
      case ViewMode.Longitudinal =>
        val projection = new LongitudinalProjection
        val transform = ViewportTransform(
          0.0, geometry.length,
          -geometry.radius, geometry.radius,
          canvas.width.value, canvas.height.value
        )
        (projection: Projection, transform)
      case ViewMode.CrossSection =>
        val projection = new CrossSectionProjection
        val margin = geometry.radius * 1.08
        val transform = ViewportTransform(
          -margin, margin,
          -margin, margin,
          canvas.width.value, canvas.height.value
        )
        (projection: Projection, transform)
    }

    trailBuffer.record(state, parameters.trailLength)
    pipeRenderer.render(gc, geometry, transform, viewMode)
    trailRenderer.render(gc, state, trailBuffer, projection, transform)
    particleRenderer.render(gc, state, projection, transform, viewMode, geometry.radius)
  }

  /** Clears accumulated render-only trails, normally after a simulation reset. */
  def clearTrails(): Unit = trailBuffer.clear()
}
