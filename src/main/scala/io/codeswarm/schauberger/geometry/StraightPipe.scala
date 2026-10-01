package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.{Vector2D, Vector3D}

/** Straight untwisted pipe with an arbitrary cross-section shape.
  *
  * @param length pipe length along the global x axis
  * @param crossSection immutable local cross-section shape
  */
final case class StraightPipe(length: Double, crossSection: CrossSectionShape) extends PipeGeometry {
  require(length > 0.0)

  /** Drops x and interprets world y-z directly as local coordinates. */
  override def toLocalCrossSection(position: Vector3D): Vector2D = Vector2D(position.y, position.z)

  /** Embeds local u-v coordinates directly into world y-z. */
  override def fromLocalCrossSection(x: Double, local: Vector2D): Vector3D = Vector3D(x, local.x, local.y)

  /** Returns the constant global x/y/z frame of an untwisted pipe. */
  override def localFrameAt(position: Vector3D): LocalFrame =
    LocalFrame(Vector3D.UnitX, Vector3D.UnitY, Vector3D.UnitZ, 0.0)

  /** Returns zero because the cross-section is not rotating. */
  override def twistRate: Double = 0.0

  /** Returns zero orientation at every axial location. */
  override def rotationAngleAt(x: Double): Double = 0.0
}
