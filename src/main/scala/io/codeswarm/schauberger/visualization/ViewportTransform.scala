package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.math.Vector2D

/** Linear transform from projected world coordinates to Canvas pixel coordinates.
  *
  * @param minX minimum projected world x
  * @param maxX maximum projected world x
  * @param minY minimum projected world y
  * @param maxY maximum projected world y
  * @param canvasWidth current canvas width in pixels
  * @param canvasHeight current canvas height in pixels
  * @param padding pixel margin around the drawable region
  */
final case class ViewportTransform(
    minX: Double,
    maxX: Double,
    minY: Double,
    maxY: Double,
    canvasWidth: Double,
    canvasHeight: Double,
    padding: Double = 24.0
) {
  require(maxX > minX, "maxX must be greater than minX")
  require(maxY > minY, "maxY must be greater than minY")

  /** Converts a projected world point to pixel coordinates with vertical inversion. */
  def worldToScreen(point: Vector2D): Vector2D = {
    val usableWidth = math.max(1.0, canvasWidth - 2.0 * padding)
    val usableHeight = math.max(1.0, canvasHeight - 2.0 * padding)
    val sx = padding + (point.x - minX) / (maxX - minX) * usableWidth
    val sy = canvasHeight - padding - (point.y - minY) / (maxY - minY) * usableHeight
    Vector2D(sx, sy)
  }
}
