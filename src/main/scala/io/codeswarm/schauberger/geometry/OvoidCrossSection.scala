package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.Vector2D

/** Smooth asymmetric ovoid represented by an angle-dependent polar radius.
  *
  * Milestone 0.5 replaces finite-difference boundary normals with an analytic
  * tangent/normal calculation, removing several repeated trigonometric calls
  * from every near-wall particle update.
  */
final case class OvoidCrossSection(width: Double, height: Double, asymmetry: Double) extends CrossSectionShape {
  require(width > 0.0 && height > 0.0)
  require(asymmetry >= -0.35 && asymmetry <= 0.35)

  private val semiWidth = width / 2.0
  private val semiHeight = height / 2.0

  /** Computes the ovoid boundary radius along one polar ray. */
  private def boundaryRadius(angle: Double): Double =
    OvoidGeometryMath.radius(angle, semiWidth, semiHeight, asymmetry)

  /** Checks whether a local point fits the ovoid boundary on its polar ray. */
  override def contains(point: Vector2D): Boolean =
    point.magnitude <= boundaryRadius(math.atan2(point.y, point.x)) + Vector2D.Epsilon

  /** Returns a positive radial boundary gap for inside points. */
  override def signedDistance(point: Vector2D): Double =
    boundaryRadius(math.atan2(point.y, point.x)) - point.magnitude

  /** Returns an analytic inward unit normal without finite differences. */
  override def inwardNormal(point: Vector2D): Vector2D = {
    if (point.magnitude <= Vector2D.Epsilon) Vector2D.Zero
    else OvoidGeometryMath.inwardNormal(math.atan2(point.y, point.x), semiWidth, semiHeight, asymmetry)
  }

  /** Projects a point radially to just inside the ovoid boundary. */
  override def clampInside(point: Vector2D, epsilon: Double): Vector2D = {
    if (contains(point) && signedDistance(point) >= epsilon) point
    else {
      val angle = math.atan2(point.y, point.x)
      val radius = math.max(0.0, boundaryRadius(angle) - epsilon)
      Vector2D(radius * math.cos(angle), radius * math.sin(angle))
    }
  }

  /** Samples an exact parametric boundary point for rendering. */
  override def boundaryPoint(angle: Double): Vector2D =
    OvoidGeometryMath.boundaryPoint(angle, semiWidth, semiHeight, asymmetry)

  /** Returns a safe maximum extent for rendering and rejection sampling. */
  override def boundingRadius: Double = math.max(semiWidth, semiHeight) * (1.0 + math.abs(asymmetry))

  /** Uses geometric mean of ellipse semi-axes as representative radial scale. */
  override def characteristicRadius: Double = math.sqrt(semiWidth * semiHeight)
}
