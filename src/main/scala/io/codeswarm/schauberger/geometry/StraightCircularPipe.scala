package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.{Vector2D, Vector3D}

/** Convenience baseline geometry for a straight circular pipe. */
final case class StraightCircularPipe(length: Double, radius: Double) extends PipeGeometry {
  require(length > 0.0 && radius > 0.0)

  override val crossSection: CrossSectionShape = CircularCrossSection(radius)

  /** Converts world y-z directly to local circle coordinates. */
  override def toLocalCrossSection(position: Vector3D): Vector2D = Vector2D(position.y, position.z)

  /** Converts local circle coordinates directly to world y-z. */
  override def fromLocalCrossSection(x: Double, local: Vector2D): Vector3D = Vector3D(x, local.x, local.y)

  /** Returns the constant untwisted world frame. */
  override def localFrameAt(position: Vector3D): LocalFrame = LocalFrame(
    origin = centerLinePosition(position.x),
    tangent = Vector3D.UnitX,
    normal = Vector3D.UnitY,
    binormal = Vector3D.UnitZ,
    rotation = Rotation2D.Identity
  )

  /** Returns zero twist rate. */
  override def twistRate: Double = 0.0

  /** Returns zero rotation angle. */
  override def rotationAngleAt(x: Double): Double = 0.0
}
