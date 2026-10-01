package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.model.{SimulationState, ViewMode}
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.paint.Color

/** Renders tracer particles on a Canvas through the selected projection. */
final class ParticleRenderer {
  /** Draws all particles and uses z depth to modulate longitudinal marker size. */
  def render(
      gc: GraphicsContext,
      state: SimulationState,
      projection: Projection,
      transform: ViewportTransform,
      viewMode: ViewMode,
      pipeRadius: Double
  ): Unit = {
    gc.fill = Color.web("#2f9de0", 0.86)
    state.particles.foreach { particle =>
      val p = transform.worldToScreen(projection.project(particle.position))
      val depth = if (viewMode == ViewMode.Longitudinal && pipeRadius > 0.0)
        math.max(-1.0, math.min(1.0, particle.position.z / pipeRadius))
      else 0.0
      val diameter = 2.6 + 1.2 * (depth + 1.0) / 2.0
      gc.fillOval(p.x - diameter / 2.0, p.y - diameter / 2.0, diameter, diameter)
    }
  }
}
