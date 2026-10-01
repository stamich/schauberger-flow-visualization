package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.{Vector2D, Vector3D}

/** Local orthonormal frame of the pipe cross-section at one axial position. */
final case class LocalFrame(
    tangent: Vector3D,
    normal: Vector3D,
    binormal: Vector3D,
    rotationAngle: Double
) {
  /** Converts a local cross-section vector `(u,v)` to world coordinates. */
  def crossSectionVectorToWorld(local: Vector2D): Vector3D = normal * local.x + binormal * local.y

  /** Projects a world vector onto local cross-section axes. */
  def worldVectorToCrossSection(world: Vector3D): Vector2D = Vector2D(world.dot(normal), world.dot(binormal))
}
