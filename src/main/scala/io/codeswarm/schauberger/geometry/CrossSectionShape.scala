package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.Vector2D

/** Two-dimensional cross-section contract independent of the pipe's axial transform. */
trait CrossSectionShape {
  /** Returns whether a local point lies inside the cross-section. */
  def contains(point: Vector2D): Boolean

  /** Approximate positive distance to the boundary for inside points, negative outside. */
  def signedDistance(point: Vector2D): Double

  /** Inward-pointing local unit normal at/near the boundary. */
  def inwardNormal(point: Vector2D): Vector2D

  /** Projects a point just inside the cross-section. */
  def clampInside(point: Vector2D, epsilon: Double = 1e-6): Vector2D

  /** Returns a boundary point for polar angle radians. */
  def boundaryPoint(angle: Double): Vector2D

  /** Returns radial occupancy where 0 is center and approximately 1 is the boundary. */
  def normalizedRadius(point: Vector2D): Double = {
    val radius = point.magnitude
    val boundary = radius + signedDistance(point)
    if (boundary <= Vector2D.Epsilon) 0.0 else radius / boundary
  }

  /** Maximum local extent used for safe rendering/sampling bounds. */
  def boundingRadius: Double

  /** Characteristic scale used by normalized metrics and swirl profiles. */
  def characteristicRadius: Double
}
