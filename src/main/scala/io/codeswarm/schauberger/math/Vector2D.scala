package io.codeswarm.schauberger.math

/** Immutable two-dimensional vector used by the simulation model.
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

  /** Divides both components by a scalar.
    *
    * @throws IllegalArgumentException when the divisor is zero
    */
  def /(scalar: Double): Vector2D = {
    require(scalar != 0.0, "Vector2D cannot be divided by zero")
    Vector2D(x / scalar, y / scalar)
  }

  /** Returns the squared vector magnitude without calculating a square root. */
  def magnitudeSquared: Double = x * x + y * y

  /** Returns the Euclidean magnitude of this vector. */
  def magnitude: Double = math.sqrt(magnitudeSquared)

  /** Returns a unit vector in the same direction, or zero for the zero vector. */
  def normalized: Vector2D = {
    val length = magnitude
    if (length == 0.0) Vector2D.Zero else this / length
  }

  /** Limits the vector magnitude while preserving its direction.
    *
    * @param max maximum allowed magnitude; must be non-negative
    */
  def limit(max: Double): Vector2D = {
    require(max >= 0.0, "Maximum magnitude must be non-negative")
    if (magnitudeSquared > max * max) normalized * max else this
  }

  /** Calculates the scalar dot product with another vector. */
  def dot(other: Vector2D): Double = x * other.x + y * other.y
}

/** Common constants for [[Vector2D]]. */
object Vector2D {
  /** Zero-length vector. */
  val Zero: Vector2D = Vector2D(0.0, 0.0)
}
