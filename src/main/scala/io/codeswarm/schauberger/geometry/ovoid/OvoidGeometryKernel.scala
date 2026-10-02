package io.codeswarm.schauberger.geometry.ovoid

import io.codeswarm.schauberger.math.Vector2D

/** Low-level strategy used by an ovoid cross-section to evaluate its boundary. */
trait OvoidGeometryKernel {
  /** Returns the radial distance from the origin to the boundary at `angle`. */
  def radiusAt(angle: Double): Double

  /** Returns the inward unit normal at the boundary point for `angle`. */
  def normalAt(angle: Double): Vector2D

  /** Returns a boundary point for `angle`. */
  def boundaryPoint(angle: Double): Vector2D
}
