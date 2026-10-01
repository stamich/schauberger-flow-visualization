package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.Vector2D

/** Circular cross-section retained as the milestone 0.1/0.2 baseline.
  *
  * @param radius circle radius in simulation units
  */
final case class CircularCrossSection(radius: Double) extends CrossSectionShape {
  require(radius > 0.0)

  /** Checks whether a point lies inside or on the circular boundary. */
  override def contains(point: Vector2D): Boolean = point.magnitude <= radius + Vector2D.Epsilon

  /** Returns radial gap `radius - |point|`. */
  override def signedDistance(point: Vector2D): Double = radius - point.magnitude

  /** Returns the radial unit direction pointing toward the center. */
  override def inwardNormal(point: Vector2D): Vector2D =
    if (point.magnitude <= Vector2D.Epsilon) Vector2D.Zero else point.normalized * -1.0

  /** Projects an outside point just inside the circular wall. */
  override def clampInside(point: Vector2D, epsilon: Double): Vector2D = {
    val maximum = math.max(0.0, radius - epsilon)
    if (point.magnitude <= maximum) point else point.normalized * maximum
  }

  /** Returns the circle boundary point at polar angle `angle`. */
  override def boundaryPoint(angle: Double): Vector2D =
    Vector2D(radius * math.cos(angle), radius * math.sin(angle))

  /** Returns the exact maximum extent of the circle. */
  override def boundingRadius: Double = radius

  /** Uses the physical circle radius as characteristic scale. */
  override def characteristicRadius: Double = radius
}
