package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.SimulationState
import scalafx.scene.canvas.Canvas

/** Facade coordinating all milestone 0.1 Canvas rendering. */
final class SimulationRenderer(
    canvas: Canvas,
    pipeRenderer: PipeRenderer,
    particleRenderer: ParticleRenderer
) {

  /** Renders the complete simulation state. */
  def render(state: SimulationState, geometry: PipeGeometry): Unit = {
    val transform = ViewportTransform(
      worldWidth = geometry.length,
      worldHalfHeight = geometry.radius,
      canvasWidth = canvas.width.value,
      canvasHeight = canvas.height.value
    )
    val gc = canvas.graphicsContext2D
    pipeRenderer.render(gc, geometry, transform, canvas.width.value, canvas.height.value)
    particleRenderer.render(gc, state, transform)
  }
}
