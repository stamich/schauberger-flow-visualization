package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.geometry.ovoid.{ExactOvoidGeometryKernel, LookupOvoidGeometryKernel, OvoidBoundaryLookup, OvoidGeometryKernel}
import io.codeswarm.schauberger.math.Vector2D

/** Smooth asymmetric ovoid represented by an angle-dependent polar radius.
 *
 * Milestone 0.6 delegates boundary evaluation to a pluggable kernel. Exact
 * mode keeps analytic equations for validation while lookup mode precomputes
 * the expensive radius/normal data and interpolates it during simulation.
 *
 * @param width          full local width of the ovoid
 * @param height         full local height of the ovoid
 * @param asymmetry      vertical polar-radius asymmetry coefficient
 * @param evaluationMode exact or lookup boundary evaluation
 * @param lookupSamples  number of samples used by lookup mode
 */
final case class OvoidCrossSection(
                                    width: Double,
                                    height: Double,
                                    asymmetry: Double,
                                    evaluationMode: GeometryEvaluationMode = GeometryEvaluationMode.Lookup,
                                    lookupSamples: Int = 1024
                                  ) extends CrossSectionShape {
  require(width > 0.0 && height > 0.0)
  require(asymmetry >= -0.35 && asymmetry <= 0.35)
  require(lookupSamples >= 32)

  private val semiWidth = width / 2.0
  private val semiHeight = height / 2.0

  private val kernel: OvoidGeometryKernel = evaluationMode match {
    case GeometryEvaluationMode.Exact =>
      new ExactOvoidGeometryKernel(semiWidth, semiHeight, asymmetry)
    case GeometryEvaluationMode.Lookup =>
      new LookupOvoidGeometryKernel(
        OvoidBoundaryLookup.build(semiWidth, semiHeight, asymmetry, lookupSamples)
      )
  }

  /** Computes the ovoid boundary radius along one polar ray. */
  private def boundaryRadius(angle: Double): Double = kernel.radiusAt(angle)

  /** Checks whether a local point fits the ovoid boundary on its polar ray. */
  override def contains(point: Vector2D): Boolean =
    point.magnitude <= boundaryRadius(math.atan2(point.y, point.x)) + Vector2D.Epsilon

  /** Returns a positive radial boundary gap for inside points. */
  override def signedDistance(point: Vector2D): Double =
    boundaryRadius(math.atan2(point.y, point.x)) - point.magnitude

  /** Returns an inward unit normal using the configured geometry kernel. */
  override def inwardNormal(point: Vector2D): Vector2D = {
    if (point.magnitude <= Vector2D.Epsilon) Vector2D.Zero
    else kernel.normalAt(math.atan2(point.y, point.x))
  }

  /** Projects a point radially to just inside the ovoid boundary. */
  override def clampInside(point: Vector2D, epsilon: Double): Vector2D = {
    val angle = math.atan2(point.y, point.x)
    val boundary = boundaryRadius(angle)
    val currentRadius = point.magnitude
    if (currentRadius <= boundary - epsilon) point
    else {
      val radius = math.max(0.0, boundary - epsilon)
      Vector2D(radius * math.cos(angle), radius * math.sin(angle))
    }
  }

  /** Samples a boundary point using the configured geometry kernel. */
  override def boundaryPoint(angle: Double): Vector2D = kernel.boundaryPoint(angle)

  /** Returns a safe maximum extent for rendering and rejection sampling. */
  override def boundingRadius: Double = math.max(semiWidth, semiHeight) * (1.0 + math.abs(asymmetry))

  /** Uses geometric mean of ellipse semi-axes as representative radial scale. */
  override def characteristicRadius: Double = math.sqrt(semiWidth * semiHeight)
}
