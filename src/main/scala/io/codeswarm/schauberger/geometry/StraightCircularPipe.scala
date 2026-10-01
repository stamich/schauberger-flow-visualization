package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.Vector3D

/** Straight cylindrical pipe aligned with the positive x axis.
  *
  * @param length total simulated axial length
  * @param radius circular cross-section radius
  */
final case class StraightCircularPipe(length: Double, radius: Double) extends PipeGeometry {
  require(length > 0.0, "Pipe length must be positive")
  require(radius > 0.0, "Pipe radius must be positive")

  /** Checks axial bounds and circular radial bounds. */
  override def contains(position: Vector3D): Boolean =
    position.x >= 0.0 && position.x <= length && radialDistance(position) <= radius

  /** Computes sqrt(y^2 + z^2). */
  override def radialDistance(position: Vector3D): Double =
    math.hypot(position.y, position.z)

  /** Returns the straight center-line point at the supplied x coordinate. */
  override def centerLinePosition(x: Double): Vector3D = Vector3D(x, 0.0, 0.0)

  /** Returns the constant straight-pipe tangent. */
  override def tangentAt(position: Vector3D): Vector3D = Vector3D.UnitX

  /** Projects an outside cross-section point back just inside the circular wall. */
  override def clampToWalls(position: Vector3D, epsilon: Double): Vector3D = {
    val radial = Vector3D(0.0, position.y, position.z)
    val distance = radial.magnitude
    val maximum = math.max(0.0, radius - epsilon)
    if (distance <= maximum || distance <= Vector3D.Epsilon) position
    else {
      val clamped = radial.normalized * maximum
      Vector3D(position.x, clamped.y, clamped.z)
    }
  }
}
