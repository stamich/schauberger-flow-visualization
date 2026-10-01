package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.math.Vector2D
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.paint.Color

/** Draws the current longitudinal pipe section. */
final class PipeRenderer {

  /** Clears the canvas, paints a subtle background, and draws both pipe walls. */
  def render(gc: GraphicsContext, geometry: PipeGeometry, transform: ViewportTransform, width: Double, height: Double): Unit = {
    gc.fill = Color.rgb(12, 18, 28)
    gc.fillRect(0.0, 0.0, width, height)

    val upperStart = transform.worldToScreen(Vector2D(0.0, geometry.radius))
    val upperEnd = transform.worldToScreen(Vector2D(geometry.length, geometry.radius))
    val lowerStart = transform.worldToScreen(Vector2D(0.0, -geometry.radius))
    val lowerEnd = transform.worldToScreen(Vector2D(geometry.length, -geometry.radius))
    val centerStart = transform.worldToScreen(Vector2D(0.0, 0.0))
    val centerEnd = transform.worldToScreen(Vector2D(geometry.length, 0.0))

    gc.stroke = Color.rgb(100, 170, 210)
    gc.lineWidth = 2.0
    gc.strokeLine(upperStart.x, upperStart.y, upperEnd.x, upperEnd.y)
    gc.strokeLine(lowerStart.x, lowerStart.y, lowerEnd.x, lowerEnd.y)

    gc.stroke = Color.rgb(48, 72, 92, 0.5)
    gc.lineWidth = 1.0
    gc.strokeLine(centerStart.x, centerStart.y, centerEnd.x, centerEnd.y)
  }
}
