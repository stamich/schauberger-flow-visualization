package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.math.{Vector2D, Vector3D}

/** World y-z projection used for a selected axial slice. */
final class CrossSectionProjection extends Projection {
  override def project(point: Vector3D): Vector2D = Vector2D(point.y, point.z)
}
