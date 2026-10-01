package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.{Vector2D, Vector3D}

/** Straight-centerline pipe whose cross-section rotates continuously along x.
  *
  * @param length pipe length along x
  * @param crossSection local shape before axial rotation
  * @param twistTurns number of complete 2π cross-section turns over the pipe length
  */
final case class TwistedPipe(length: Double, crossSection: CrossSectionShape, twistTurns: Double) extends PipeGeometry {
  require(length > 0.0 && twistTurns >= 0.0)

  /** Computes `theta(x) = 2π * twistTurns * x / length`, clamped to the pipe. */
  override def rotationAngleAt(x: Double): Double =
    2.0 * math.Pi * twistTurns * math.max(0.0, math.min(length, x)) / length

  /** Returns constant cross-section twist rate in radians per axial unit. */
  override def twistRate: Double = 2.0 * math.Pi * twistTurns / length

  /** Removes the local cross-section rotation from world y-z coordinates. */
  override def toLocalCrossSection(position: Vector3D): Vector2D =
    Vector2D(position.y, position.z).rotate(-rotationAngleAt(position.x))

  /** Applies the local cross-section rotation and embeds local coordinates in world space. */
  override def fromLocalCrossSection(x: Double, local: Vector2D): Vector3D = {
    val world = local.rotate(rotationAngleAt(x))
    Vector3D(x, world.x, world.y)
  }

  /** Returns tangent plus rotated local normal/binormal basis vectors. */
  override def localFrameAt(position: Vector3D): LocalFrame = {
    val theta = rotationAngleAt(position.x)
    val cosine = math.cos(theta)
    val sine = math.sin(theta)
    LocalFrame(
      tangent = Vector3D.UnitX,
      normal = Vector3D(0.0, cosine, sine),
      binormal = Vector3D(0.0, -sine, cosine),
      rotationAngle = theta
    )
  }
}
