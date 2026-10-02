package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.{Vector2D, Vector3D}

/** Local orthonormal frame of the pipe cross-section at one axial position.
 *
 * @param origin   center-line point of the local cross-section
 * @param tangent  local axial unit direction
 * @param normal   first local cross-section basis vector
 * @param binormal second local cross-section basis vector
 * @param rotation cached cross-section rotation
 */
final case class LocalFrame(
                             origin: Vector3D,
                             tangent: Vector3D,
                             normal: Vector3D,
                             binormal: Vector3D,
                             rotation: Rotation2D
                           ) {
  /** Converts a local cross-section vector `(u,v)` to world coordinates. */
  def crossSectionVectorToWorld(local: Vector2D): Vector3D = normal * local.x + binormal * local.y

  /** Projects a world vector onto local cross-section axes. */
  def worldVectorToCrossSection(world: Vector3D): Vector2D = Vector2D(world.dot(normal), world.dot(binormal))

  /** Converts a local cross-section point to world coordinates at this frame. */
  def localPointToWorld(local: Vector2D): Vector3D = origin + crossSectionVectorToWorld(local)

  /** Converts a world point into local cross-section coordinates. */
  def worldPointToLocal(world: Vector3D): Vector2D = worldVectorToCrossSection(world - origin)

  /** Local twist angle in radians. */
  def rotationAngle: Double = rotation.angle
}
