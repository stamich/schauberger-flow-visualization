package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.math.{Vector2D, Vector3D}

/** Maps a 3D simulation point into a 2D visualization plane. */
trait Projection {
  /** Projects one world point into two-dimensional world coordinates. */
  def project(point: Vector3D): Vector2D
}
