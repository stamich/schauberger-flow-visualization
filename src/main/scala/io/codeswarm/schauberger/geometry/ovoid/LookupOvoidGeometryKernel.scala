package io.codeswarm.schauberger.geometry.ovoid

import io.codeswarm.schauberger.math.Vector2D

/** Lookup-accelerated ovoid boundary implementation. */
final class LookupOvoidGeometryKernel(lookup: OvoidBoundaryLookup) extends OvoidGeometryKernel {
  /** Returns interpolated radial boundary distance. */
  override def radiusAt(angle: Double): Double = lookup.radiusAt(angle)

  /** Returns interpolated inward boundary normal. */
  override def normalAt(angle: Double): Vector2D = lookup.normalAt(angle)

  /** Returns a boundary point from the interpolated radius. */
  override def boundaryPoint(angle: Double): Vector2D = {
    val radius = lookup.radiusAt(angle)
    Vector2D(radius * math.cos(angle), radius * math.sin(angle))
  }
}
