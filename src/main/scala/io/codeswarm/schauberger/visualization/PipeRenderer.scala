package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.math.Vector2D
import io.codeswarm.schauberger.model.{ViewMode, VisualizationParameters}
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.paint.Color

/** Geometry-aware pipe renderer for longitudinal and selected cross-section views. */
final class PipeRenderer {
  def render(gc: GraphicsContext, geometry: PipeGeometry, transform: ViewportTransform, visualization: VisualizationParameters, sliceX: Double): Unit = {
    gc.stroke = Color.Gray
    gc.lineWidth = 2.0
    visualization.viewMode match {
      case ViewMode.Longitudinal =>
        val extent = geometry.boundingRadius
        val topA = transform.worldToScreen(Vector2D(0.0, extent))
        val topB = transform.worldToScreen(Vector2D(geometry.length, extent))
        val botA = transform.worldToScreen(Vector2D(0.0, -extent))
        val botB = transform.worldToScreen(Vector2D(geometry.length, -extent))
        gc.strokeLine(topA.x, topA.y, topB.x, topB.y)
        gc.strokeLine(botA.x, botA.y, botB.x, botB.y)
        renderTwistMarkers(gc, geometry, transform)
      case ViewMode.CrossSection => renderCrossSection(gc, geometry, transform, sliceX)
    }
  }

  /** Samples and draws the actual rotated cross-section boundary. */
  private def renderCrossSection(gc: GraphicsContext, geometry: PipeGeometry, transform: ViewportTransform, x: Double): Unit = {
    val samples = 128
    val points = (0 to samples).map { i =>
      val local = geometry.crossSection.boundaryPoint(2.0 * math.Pi * i / samples.toDouble)
      val world = geometry.fromLocalCrossSection(x, local)
      transform.worldToScreen(Vector2D(world.y, world.z))
    }
    gc.beginPath()
    gc.moveTo(points.head.x, points.head.y)
    points.tail.foreach(p => gc.lineTo(p.x, p.y))
    gc.closePath()
    gc.stroke()
  }

  /** Shows sparse local-orientation markers when the cross-section twists. */
  private def renderTwistMarkers(gc: GraphicsContext, geometry: PipeGeometry, transform: ViewportTransform): Unit = {
    if (math.abs(geometry.twistRate) > 1e-12) {
      gc.stroke = Color.web("#94a3b8", 0.35)
      gc.lineWidth = 0.7
      (0 to 12).foreach { i =>
        val x = geometry.length * i / 12.0
        val angle = geometry.rotationAngleAt(x)
        val half = geometry.boundingRadius * 0.22
        val a = Vector2D(x, math.sin(angle) * half)
        val b = Vector2D(x, -math.sin(angle) * half)
        val sa = transform.worldToScreen(a)
        val sb = transform.worldToScreen(b)
        gc.strokeLine(sa.x, sa.y, sb.x, sb.y)
      }
    }
  }
}
