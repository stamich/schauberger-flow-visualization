package io.codeswarm.schauberger.geometry.ovoid

import io.codeswarm.schauberger.geometry.OvoidGeometryMath
import io.codeswarm.schauberger.math.Vector2D

/** Exact analytic implementation of the ovoid boundary equations. */
final class ExactOvoidGeometryKernel(
    semiWidth: Double,
    semiHeight: Double,
    asymmetry: Double
) extends OvoidGeometryKernel {
  require(semiWidth > 0.0 && semiHeight > 0.0)

  /** Evaluates the analytic ovoid radius. */
  override def radiusAt(angle: Double): Double =
    OvoidGeometryMath.radius(angle, semiWidth, semiHeight, asymmetry)

  /** Evaluates the analytic inward normal. */
  override def normalAt(angle: Double): Vector2D =
    OvoidGeometryMath.inwardNormal(angle, semiWidth, semiHeight, asymmetry)

  /** Evaluates an exact boundary point. */
  override def boundaryPoint(angle: Double): Vector2D =
    OvoidGeometryMath.boundaryPoint(angle, semiWidth, semiHeight, asymmetry)
}
