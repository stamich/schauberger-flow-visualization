package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.math.Vector2D

/** Maps simulation/world coordinates to JavaFX Canvas coordinates.
  *
  * World y coordinates are centered around zero and increase upward. Canvas y
  * coordinates increase downward, so the transform flips the y axis.
  */
final case class ViewportTransform(
    worldWidth: Double,
    worldHalfHeight: Double,
    canvasWidth: Double,
    canvasHeight: Double,
    horizontalPadding: Double = 24.0,
    verticalPadding: Double = 32.0
) {
  require(worldWidth > 0.0, "worldWidth must be positive")
  require(worldHalfHeight > 0.0, "worldHalfHeight must be positive")

  private val usableWidth = math.max(1.0, canvasWidth - horizontalPadding * 2.0)
  private val usableHeight = math.max(1.0, canvasHeight - verticalPadding * 2.0)
  private val xScale = usableWidth / worldWidth
  private val yScale = usableHeight / (worldHalfHeight * 2.0)

  /** Maps one simulation point into Canvas coordinates. */
  def worldToScreen(position: Vector2D): Vector2D =
    Vector2D(
      horizontalPadding + position.x * xScale,
      canvasHeight / 2.0 - position.y * yScale
    )

  /** Converts a world-space vertical distance into pixels. */
  def worldDistanceToScreen(distance: Double): Double = distance * yScale
}
