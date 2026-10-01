package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.Vector2D

/** Smooth asymmetric ovoid represented by an angle-dependent polar radius.
  *
  * The base is an ellipse with semi-axes `width / 2` and `height / 2`.
  * `asymmetry` modulates the radius by `(1 + asymmetry * sin(angle))`, producing
  * a wider upper or lower lobe while remaining stable and radially invertible.
  *
  * @param width full horizontal diameter of the underlying ellipse
  * @param height full vertical diameter of the underlying ellipse
  * @param asymmetry bounded vertical asymmetry in `[-0.35, 0.35]`
  */
final case class OvoidCrossSection(width: Double, height: Double, asymmetry: Double) extends CrossSectionShape {
  require(width > 0.0 && height > 0.0)
  require(asymmetry >= -0.35 && asymmetry <= 0.35)

  private val semiWidth = width / 2.0
  private val semiHeight = height / 2.0

  /** Computes ovoid boundary radius along one polar ray. */
  private def boundaryRadius(angle: Double): Double = {
    val cosine = math.cos(angle)
    val sine = math.sin(angle)
    val ellipseRadius = 1.0 / math.sqrt(
      cosine * cosine / (semiWidth * semiWidth) + sine * sine / (semiHeight * semiHeight)
    )
    ellipseRadius * (1.0 + asymmetry * sine)
  }

  /** Checks whether a local point fits the ovoid boundary on its polar ray. */
  override def contains(point: Vector2D): Boolean =
    point.magnitude <= boundaryRadius(math.atan2(point.y, point.x)) + Vector2D.Epsilon

  /** Returns an approximate radial signed gap to the ovoid boundary. */
  override def signedDistance(point: Vector2D): Double =
    boundaryRadius(math.atan2(point.y, point.x)) - point.magnitude

  /** Estimates the inward normal from the gradient of the signed-distance proxy. */
  override def inwardNormal(point: Vector2D): Vector2D = {
    if (point.magnitude <= Vector2D.Epsilon) Vector2D.Zero
    else {
      val h = math.max(1e-4, characteristicRadius * 1e-4)
      val dx = signedDistance(Vector2D(point.x + h, point.y)) - signedDistance(Vector2D(point.x - h, point.y))
      val dy = signedDistance(Vector2D(point.x, point.y + h)) - signedDistance(Vector2D(point.x, point.y - h))
      Vector2D(dx, dy).normalized
    }
  }

  /** Projects a point radially to just inside the local ovoid boundary. */
  override def clampInside(point: Vector2D, epsilon: Double): Vector2D = {
    if (contains(point) && signedDistance(point) >= epsilon) point
    else {
      val angle = math.atan2(point.y, point.x)
      val radius = math.max(0.0, boundaryRadius(angle) - epsilon)
      Vector2D(radius * math.cos(angle), radius * math.sin(angle))
    }
  }

  /** Samples an exact parametric boundary point for rendering. */
  override def boundaryPoint(angle: Double): Vector2D = {
    val radius = boundaryRadius(angle)
    Vector2D(radius * math.cos(angle), radius * math.sin(angle))
  }

  /** Returns a safe maximum extent for rendering and rejection sampling. */
  override def boundingRadius: Double =
    math.max(semiWidth, semiHeight) * (1.0 + math.abs(asymmetry))

  /** Uses geometric mean of ellipse semi-axes as representative radial scale. */
  override def characteristicRadius: Double = math.sqrt(semiWidth * semiHeight)
}
