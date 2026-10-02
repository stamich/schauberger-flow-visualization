package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.math.Vector2D

/** Linear world-to-screen mapping with inverted screen y axis. */
final case class ViewportTransform(minX: Double, maxX: Double, minY: Double, maxY: Double, canvasWidth: Double, canvasHeight: Double) {
  require(maxX > minX && maxY > minY && canvasWidth > 0.0 && canvasHeight > 0.0)

  def worldToScreen(point: Vector2D): Vector2D = {
    val sx = (point.x - minX) / (maxX - minX) * canvasWidth
    val sy = canvasHeight - (point.y - minY) / (maxY - minY) * canvasHeight
    Vector2D(sx, sy)
  }
}
