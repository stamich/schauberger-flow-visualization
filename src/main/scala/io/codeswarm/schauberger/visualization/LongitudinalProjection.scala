package io.codeswarm.schauberger.visualization

import io.codeswarm.schauberger.math.{Vector2D, Vector3D}
/** Side projection x-y. */
final class LongitudinalProjection extends Projection { override def project(point: Vector3D): Vector2D = Vector2D(point.x, point.y) }
