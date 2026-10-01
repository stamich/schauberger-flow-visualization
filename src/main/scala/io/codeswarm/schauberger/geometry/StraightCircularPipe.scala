package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.Vector2D

/** Straight pipe represented as a 2D longitudinal section of a cylinder.
  *
  * @param length total simulated pipe length
  * @param radius distance between the center line and either wall
  */
final case class StraightCircularPipe(length: Double, radius: Double) extends PipeGeometry {
  require(length > 0.0, "Pipe length must be positive")
  require(radius > 0.0, "Pipe radius must be positive")

  /** Checks both axial bounds and upper/lower wall bounds. */
  override def contains(position: Vector2D): Boolean =
    position.x >= 0.0 && position.x <= length && math.abs(position.y) <= radius

  /** Returns the absolute y distance from the pipe center line. */
  override def distanceFromCenter(position: Vector2D): Double = math.abs(position.y)

  /** Clamps only the y coordinate; outlet/inlet handling is a boundary concern. */
  override def clampToWalls(position: Vector2D): Vector2D =
    position.copy(y = math.max(-radius, math.min(radius, position.y)))
}
