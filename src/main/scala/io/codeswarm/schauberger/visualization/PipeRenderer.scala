package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.math.Vector2D
import io.codeswarm.schauberger.model.ViewMode
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.paint.Color

/** Draws pipe boundaries for both supported milestone 0.2 projections. */
final class PipeRenderer {
  /** Draws two wall lines in longitudinal mode or a circle in cross-section mode. */
  def render(
      gc: GraphicsContext,
      geometry: PipeGeometry,
      transform: ViewportTransform,
      viewMode: ViewMode
  ): Unit = {
    gc.stroke = Color.Gray
    gc.lineWidth = 2.0
    viewMode match {
      case ViewMode.Longitudinal =>
        val a = transform.worldToScreen(Vector2D(0.0, geometry.radius))
        val b = transform.worldToScreen(Vector2D(geometry.length, geometry.radius))
        val c = transform.worldToScreen(Vector2D(0.0, -geometry.radius))
        val d = transform.worldToScreen(Vector2D(geometry.length, -geometry.radius))
        gc.strokeLine(a.x, a.y, b.x, b.y)
        gc.strokeLine(c.x, c.y, d.x, d.y)
      case ViewMode.CrossSection =>
        val topLeft = transform.worldToScreen(Vector2D(-geometry.radius, geometry.radius))
        val bottomRight = transform.worldToScreen(Vector2D(geometry.radius, -geometry.radius))
        gc.strokeOval(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y)
    }
  }
}
