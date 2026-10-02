package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.Vector2D

/** Analytic helpers for the polar ovoid used by [[OvoidCrossSection]]. */
private[geometry] object OvoidGeometryMath {
  /** Computes ellipse radius on a ray. */
  private def ellipseRadius(angle: Double, semiWidth: Double, semiHeight: Double): Double = {
    val c = math.cos(angle)
    val s = math.sin(angle)
    1.0 / math.sqrt(c * c / (semiWidth * semiWidth) + s * s / (semiHeight * semiHeight))
  }

  /** Computes the derivative of ellipse radius with respect to polar angle. */
  private def ellipseRadiusDerivative(angle: Double, semiWidth: Double, semiHeight: Double): Double = {
    val c = math.cos(angle)
    val s = math.sin(angle)
    val q = c * c / (semiWidth * semiWidth) + s * s / (semiHeight * semiHeight)
    val qPrime = 2.0 * c * s * (1.0 / (semiHeight * semiHeight) - 1.0 / (semiWidth * semiWidth))
    -0.5 * qPrime / math.pow(q, 1.5)
  }

  /** Computes asymmetric ovoid radius. */
  def radius(angle: Double, semiWidth: Double, semiHeight: Double, asymmetry: Double): Double = {
    val e = ellipseRadius(angle, semiWidth, semiHeight)
    e * (1.0 + asymmetry * math.sin(angle))
  }

  /** Computes the derivative of asymmetric ovoid radius. */
  def radiusDerivative(angle: Double, semiWidth: Double, semiHeight: Double, asymmetry: Double): Double = {
    val e = ellipseRadius(angle, semiWidth, semiHeight)
    val ep = ellipseRadiusDerivative(angle, semiWidth, semiHeight)
    val s = math.sin(angle)
    val c = math.cos(angle)
    ep * (1.0 + asymmetry * s) + e * asymmetry * c
  }

  /** Computes a boundary point at a polar angle. */
  def boundaryPoint(angle: Double, semiWidth: Double, semiHeight: Double, asymmetry: Double): Vector2D = {
    val r = radius(angle, semiWidth, semiHeight, asymmetry)
    Vector2D(r * math.cos(angle), r * math.sin(angle))
  }

  /** Computes an analytic inward unit normal on the polar boundary. */
  def inwardNormal(angle: Double, semiWidth: Double, semiHeight: Double, asymmetry: Double): Vector2D = {
    val r = radius(angle, semiWidth, semiHeight, asymmetry)
    val rp = radiusDerivative(angle, semiWidth, semiHeight, asymmetry)
    val c = math.cos(angle)
    val s = math.sin(angle)
    val dx = rp * c - r * s
    val dy = rp * s + r * c
    Vector2D(-dy, dx).normalized
  }
}
