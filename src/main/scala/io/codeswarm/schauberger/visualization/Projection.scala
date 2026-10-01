package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.math.{Vector2D, Vector3D}
/** Maps 3D simulation coordinates into a two-dimensional view. */
trait Projection { def project(point: Vector3D): Vector2D }
