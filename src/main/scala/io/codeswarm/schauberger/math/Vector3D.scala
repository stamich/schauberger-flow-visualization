package io.codeswarm.schauberger.math

/** Immutable three-dimensional vector used by the simulation kernel.
  *
  * Coordinates follow the convention used throughout milestone 0.2:
  * x is the pipe axis, while y and z span the circular cross-section.
  *
  * @param x axial component
  * @param y first cross-section component
  * @param z second cross-section component
  */
final case class Vector3D(x: Double, y: Double, z: Double) {
  /** Adds another vector component-wise. */
  def +(other: Vector3D): Vector3D = Vector3D(x + other.x, y + other.y, z + other.z)

  /** Subtracts another vector component-wise. */
  def -(other: Vector3D): Vector3D = Vector3D(x - other.x, y - other.y, z - other.z)

  /** Multiplies all components by a scalar. */
  def *(scalar: Double): Vector3D = Vector3D(x * scalar, y * scalar, z * scalar)

  /** Divides all components by a non-zero scalar. */
  def /(scalar: Double): Vector3D = {
    require(scalar != 0.0, "Vector3D cannot be divided by zero")
    Vector3D(x / scalar, y / scalar, z / scalar)
  }

  /** Returns the squared Euclidean magnitude without calculating a square root. */
  def magnitudeSquared: Double = x * x + y * y + z * z

  /** Returns the Euclidean magnitude. */
  def magnitude: Double = math.sqrt(magnitudeSquared)

  /** Returns a unit vector with the same direction or zero for the zero vector. */
  def normalized: Vector3D = {
    val length = magnitude
    if (length <= Vector3D.Epsilon) Vector3D.Zero else this / length
  }

  /** Returns the scalar dot product with another vector. */
  def dot(other: Vector3D): Double = x * other.x + y * other.y + z * other.z

  /** Returns the right-handed cross product with another vector. */
  def cross(other: Vector3D): Vector3D = Vector3D(
    y * other.z - z * other.y,
    z * other.x - x * other.z,
    x * other.y - y * other.x
  )

  /** Limits the vector magnitude while preserving direction. */
  def limit(max: Double): Vector3D = {
    require(max >= 0.0, "Maximum magnitude must be non-negative")
    if (magnitudeSquared > max * max) normalized * max else this
  }
}

/** Common [[Vector3D]] constants. */
object Vector3D {
  /** Numerical epsilon used when normalizing vectors. */
  val Epsilon: Double = 1e-12

  /** Zero vector. */
  val Zero: Vector3D = Vector3D(0.0, 0.0, 0.0)

  /** Unit vector along the straight-pipe axis. */
  val UnitX: Vector3D = Vector3D(1.0, 0.0, 0.0)
}
