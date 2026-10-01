package io.codeswarm.schauberger.geometry

import io.codeswarm.schauberger.math.Vector2D

/** Precomputed two-dimensional rotation used by twisted cross-section transforms.
  *
  * Caching sine and cosine avoids repeating trigonometric work in several flow
  * components during the same particle update.
  */
final case class Rotation2D(angle: Double, sine: Double, cosine: Double) {
  /** Rotates a local vector counter-clockwise into the rotated frame. */
  def apply(vector: Vector2D): Vector2D =
    Vector2D(vector.x * cosine - vector.y * sine, vector.x * sine + vector.y * cosine)

  /** Applies the inverse rotation. */
  def inverse(vector: Vector2D): Vector2D =
    Vector2D(vector.x * cosine + vector.y * sine, -vector.x * sine + vector.y * cosine)
}

/** Factory helpers for [[Rotation2D]]. */
object Rotation2D {
  /** Builds a cached rotation from an angle in radians. */
  def fromAngle(angle: Double): Rotation2D = Rotation2D(angle, math.sin(angle), math.cos(angle))

  /** Identity rotation. */
  val Identity: Rotation2D = Rotation2D(0.0, 0.0, 1.0)
}
