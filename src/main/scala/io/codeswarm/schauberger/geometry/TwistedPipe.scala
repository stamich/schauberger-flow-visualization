package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.{Vector2D, Vector3D}

/** Straight-centerline pipe whose cross-section rotates continuously along x. */
final case class TwistedPipe(length: Double, crossSection: CrossSectionShape, twistTurns: Double) extends PipeGeometry {
  require(length > 0.0 && twistTurns >= 0.0)

  /** Computes `theta(x) = 2π * twistTurns * x / length`, clamped to the pipe. */
  override def rotationAngleAt(x: Double): Double =
    2.0 * math.Pi * twistTurns * math.max(0.0, math.min(length, x)) / length

  /** Returns constant cross-section twist rate in radians per axial unit. */
  override def twistRate: Double = 2.0 * math.Pi * twistTurns / length

  /** Removes local cross-section rotation from world y-z coordinates. */
  override def toLocalCrossSection(position: Vector3D): Vector2D = {
    val rotation = Rotation2D.fromAngle(rotationAngleAt(position.x))
    rotation.inverse(Vector2D(position.y, position.z))
  }

  /** Applies local cross-section rotation and embeds local coordinates in world space. */
  override def fromLocalCrossSection(x: Double, local: Vector2D): Vector3D = {
    val rotated = Rotation2D.fromAngle(rotationAngleAt(x))(local)
    Vector3D(x, rotated.x, rotated.y)
  }

  /** Returns a cached local frame for the supplied axial position. */
  override def localFrameAt(position: Vector3D): LocalFrame = {
    val rotation = Rotation2D.fromAngle(rotationAngleAt(position.x))
    LocalFrame(
      origin = centerLinePosition(position.x),
      tangent = Vector3D.UnitX,
      normal = Vector3D(0.0, rotation.cosine, rotation.sine),
      binormal = Vector3D(0.0, -rotation.sine, rotation.cosine),
      rotation = rotation
    )
  }
}
