package io.codeswarm.schauberger.math

/** Immutable two-dimensional vector used by screen projections.
  *
  * @param x horizontal component
  * @param y vertical component
  */
final case class Vector2D(x: Double, y: Double) {
  /** Adds another vector component-wise. */
  def +(other: Vector2D): Vector2D = Vector2D(x + other.x, y + other.y)

  /** Subtracts another vector component-wise. */
  def -(other: Vector2D): Vector2D = Vector2D(x - other.x, y - other.y)

  /** Multiplies both components by a scalar. */
  def *(scalar: Double): Vector2D = Vector2D(x * scalar, y * scalar)

  /** Returns the squared Euclidean magnitude. */
  def magnitudeSquared: Double = x * x + y * y

  /** Returns the Euclidean magnitude. */
  def magnitude: Double = math.sqrt(magnitudeSquared)
}

/** Common [[Vector2D]] constants. */
object Vector2D {
  /** Zero vector. */
  val Zero: Vector2D = Vector2D(0.0, 0.0)
}
