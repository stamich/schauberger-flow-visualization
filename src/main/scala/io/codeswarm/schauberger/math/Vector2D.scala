package io.codeswarm.schauberger.math

/** Immutable two-dimensional vector used by cross-section geometry and screen projections. */
final case class Vector2D(x: Double, y: Double) {
  /** Adds another vector component-wise. */
  def +(other: Vector2D): Vector2D = Vector2D(x + other.x, y + other.y)

  /** Subtracts another vector component-wise. */
  def -(other: Vector2D): Vector2D = Vector2D(x - other.x, y - other.y)

  /** Multiplies this vector by a scalar. */
  def *(scalar: Double): Vector2D = Vector2D(x * scalar, y * scalar)

  /** Divides this vector by a non-zero scalar. */
  def /(scalar: Double): Vector2D = {
    require(math.abs(scalar) > Vector2D.Epsilon)
    Vector2D(x / scalar, y / scalar)
  }

  /** Squared Euclidean magnitude. */
  def magnitudeSquared: Double = x * x + y * y

  /** Euclidean magnitude. */
  def magnitude: Double = math.sqrt(magnitudeSquared)

  /** Returns a unit vector or zero for a near-zero input. */
  def normalized: Vector2D = if (magnitude <= Vector2D.Epsilon) Vector2D.Zero else this / magnitude

  /** Dot product. */
  def dot(other: Vector2D): Double = x * other.x + y * other.y

  /** Rotates the vector counter-clockwise by angle radians. */
  def rotate(angle: Double): Vector2D = {
    val c = math.cos(angle)
    val s = math.sin(angle)
    Vector2D(x * c - y * s, x * s + y * c)
  }
}

/** Common constants for [[Vector2D]]. */
object Vector2D {
  val Epsilon: Double = 1e-9
  val Zero: Vector2D = Vector2D(0.0, 0.0)
}
