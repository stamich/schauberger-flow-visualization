package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.math.{Vector2D, Vector3D}

/** Side projection preserving axial x and cross-section y coordinates. */
final class LongitudinalProjection extends Projection {
  /** Projects (x, y, z) to (x, y). */
  override def project(point: Vector3D): Vector2D = Vector2D(point.x, point.y)
}
