package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.geometry.PipeGeometry
import io.codeswarm.schauberger.math.Vector2D
import io.codeswarm.schauberger.model.SecondaryFlowParameters
import io.codeswarm.schauberger.simulation.SecondaryFlowFieldSampler
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.paint.Color

/** Draws geometry-induced secondary-flow vectors in cross-section view. */
final class VectorFieldRenderer(fieldSampler: SecondaryFlowFieldSampler) {
  /** Samples the local field and renders compact arrows in world-oriented cross-section coordinates. */
  def render(
              gc: GraphicsContext,
              geometry: PipeGeometry,
              transform: ViewportTransform,
              axialPosition: Double,
              parameters: SecondaryFlowParameters,
              resolution: Int
            ): Unit = {
    if (!parameters.enabled) return
    val frame = geometry.localFrameAt(geometry.centerLinePosition(axialPosition))
    val samples = fieldSampler.sample(geometry, axialPosition, parameters, resolution)
    val maxSpeed = samples.foldLeft(0.0)((max, sample) => math.max(max, sample.velocity.magnitude))
    if (maxSpeed <= 1e-9) return

    gc.stroke = Color.web("#67e8f9", 0.62)
    gc.lineWidth = 1.0
    val arrowLength = math.max(4.0, transform.canvasWidth / math.max(25.0, resolution.toDouble * 3.0))

    samples.foreach { sample =>
      val pointWorld = geometry.fromLocalCrossSection(axialPosition, sample.position)
      val vectorWorld = frame.crossSectionVectorToWorld(sample.velocity)
      val start = transform.worldToScreen(Vector2D(pointWorld.y, pointWorld.z))
      val yz = Vector2D(vectorWorld.y, vectorWorld.z)
      if (yz.magnitude > 1e-9) {
        val direction = yz.normalized
        val scaled = direction * (arrowLength * sample.velocity.magnitude / maxSpeed)
        val endWorld = Vector2D(pointWorld.y + scaled.x, pointWorld.z + scaled.y)
        val end = transform.worldToScreen(endWorld)
        gc.strokeLine(start.x, start.y, end.x, end.y)
        drawHead(gc, start, end)
      }
    }
  }

  /** Draws a small two-segment arrow head in screen coordinates. */
  private def drawHead(gc: GraphicsContext, start: Vector2D, end: Vector2D): Unit = {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val length = math.sqrt(dx * dx + dy * dy)
    if (length > 2.0) {
      val ux = dx / length
      val uy = dy / length
      val head = math.min(4.0, length * 0.45)
      val px = -uy
      val py = ux
      gc.strokeLine(end.x, end.y, end.x - ux * head + px * head * 0.5, end.y - uy * head + py * head * 0.5)
      gc.strokeLine(end.x, end.y, end.x - ux * head - px * head * 0.5, end.y - uy * head - py * head * 0.5)
    }
  }
}
