package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.math.{Vector2D, Vector3D}

/** Cross-section projection preserving y and z coordinates. */
final class CrossSectionProjection extends Projection {
  /** Projects (x, y, z) to (y, z). */
  override def project(point: Vector3D): Vector2D = Vector2D(point.y, point.z)
}
