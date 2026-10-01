package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.model.{SimulationState, ViewMode, VisualizationParameters}
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.paint.Color

/** Draws current tracer positions, filtering cross-section view to a physical axial slice. */
final class ParticleRenderer {
  def render(gc: GraphicsContext, state: SimulationState, geometry: PipeGeometry, projection: Projection, transform: ViewportTransform, visualization: VisualizationParameters, sliceX: Double): Unit = {
    gc.fill = Color.web("#2f9de0", 0.88)
    state.particles.iterator.filter { p => visualization.viewMode == ViewMode.Longitudinal || math.abs(p.position.x - sliceX) <= visualization.crossSectionSliceHalfWidth }.foreach { p =>
      val screen = transform.worldToScreen(projection.project(p.position))
      val depth = if (visualization.viewMode == ViewMode.Longitudinal) math.max(-1.0, math.min(1.0, p.position.z / geometry.boundingRadius)) else 0.0
      val diameter = 2.4 + 1.0 * (depth + 1.0) / 2.0
      gc.fillOval(screen.x - diameter / 2.0, screen.y - diameter / 2.0, diameter, diameter)
    }
  }
}
