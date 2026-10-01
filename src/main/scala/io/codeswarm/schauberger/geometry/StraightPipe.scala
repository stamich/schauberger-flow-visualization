package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.{Vector2D, Vector3D}

/** Straight untwisted pipe with an arbitrary cross-section shape. */
final case class StraightPipe(length: Double, crossSection: CrossSectionShape) extends PipeGeometry {
  require(length > 0.0)

  /** Converts world y-z coordinates directly to local coordinates. */
  override def toLocalCrossSection(position: Vector3D): Vector2D = Vector2D(position.y, position.z)

  /** Embeds local u-v coordinates directly into world y-z coordinates. */
  override def fromLocalCrossSection(x: Double, local: Vector2D): Vector3D = Vector3D(x, local.x, local.y)

  /** Returns the constant untwisted local frame. */
  override def localFrameAt(position: Vector3D): LocalFrame = LocalFrame(
    origin = centerLinePosition(position.x),
    tangent = Vector3D.UnitX,
    normal = Vector3D.UnitY,
    binormal = Vector3D.UnitZ,
    rotation = Rotation2D.Identity
  )

  /** Returns zero twist rate. */
  override def twistRate: Double = 0.0

  /** Returns zero orientation at every axial position. */
  override def rotationAngleAt(x: Double): Double = 0.0
}
